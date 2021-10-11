package me.sttimort.aot3plsa

import smile.math.matrix.Matrix
import kotlin.math.abs

class FittedPlsaModel(
    private val wordTopicProbabilityMatrix: Matrix,
    private val corpus: Corpus,
    private val topicsCount: Int,
) {
    fun topNWordsPerTopics(n: Int): List<List<String>> = (0 until topicsCount).map { topicIndex ->
        wordTopicProbabilityMatrix.col(topicIndex)
            .asSequence()
            .mapIndexed { wordIndex, probability -> Pair(wordIndex, probability) }
            .sortedByDescending { (_, probability) -> probability }
            .map { (wordIndex, _) -> corpus.vocabulary.getWordByIndex(wordIndex) }
            .filterNotNull()
            .take(n)
            .toList()
    }
}

fun buildFittedPlsaModel(corpus: Corpus, topicsCount: Int): FittedPlsaModel {
    val wordDocumentCountMatrix =
        matrix(rows = corpus.vocabulary.size, columns = corpus.documentsCount) { wordIndex, documentIndex ->
            corpus.countWordDocumentInclusions(wordIndex = wordIndex, documentIndex = documentIndex).toDouble()
        }

    val wordTopicsProbabilityMatrix =
        matrix(rows = corpus.vocabulary.size, columns = topicsCount) { rowNum, colNum ->
            if (rowNum == nrows() - 1) 1 - col(colNum).sum()
            else randomDoubleNoZero() / nrows()
        }

    val topicDocumentProbabilityMatrix =
        matrix(rows = topicsCount, columns = corpus.documentsCount) { rowNum, colNum ->
            if (colNum == ncols() - 1) 1 - row(rowNum).sum()
            else randomDoubleNoZero() / ncols()
        }

    val model = PlsaModel(
        wordTopicProbabilityMatrix = wordTopicsProbabilityMatrix,
        topicDocumentProbabilityMatrix = topicDocumentProbabilityMatrix,
        wordDocumentCountMatrix = wordDocumentCountMatrix,
        topicsCount = topicsCount,
    )
    model.fit()

    return FittedPlsaModel(model.wordTopicProbabilityMatrix, corpus, topicsCount)
}


private const val LN_LIKELY_HOOD_DELTA_LIMIT = 0.1

private class PlsaModel(
    val wordTopicProbabilityMatrix: Matrix,
    val topicDocumentProbabilityMatrix: Matrix,
    private val wordDocumentCountMatrix: Matrix,
    private val topicsCount: Int,
) {
    fun fit() {
        var iteration = 0
        var delta: Double
        var oldLnLikelyHood = lnLikelyHood()
        log.info { "Fitting pLSA model. Initial lnLikelyHood = $oldLnLikelyHood" }
        do {
            performEmAlgorithmIteration()
            val newLnLikelyHood = lnLikelyHood()
            delta = newLnLikelyHood - oldLnLikelyHood
            oldLnLikelyHood = newLnLikelyHood

            log.info { "After iteration $iteration: lnLikelyHood = $oldLnLikelyHood" }
            iteration++
        } while (abs(delta) > LN_LIKELY_HOOD_DELTA_LIMIT)
    }

    private fun lnLikelyHood(): Double =
        wordTopicProbabilityMatrix.multiply(topicDocumentProbabilityMatrix).mul(wordDocumentCountMatrix).sum()

    private fun performEmAlgorithmIteration() {
        val vocabularySize = wordTopicProbabilityMatrix.nrows()
        val documentsCount = topicDocumentProbabilityMatrix.ncols()

        val wordDocumentProbabilityMatrix = wordTopicProbabilityMatrix.multiply(topicDocumentProbabilityMatrix)
        val wordPerTopicMeans = matrix(rows = vocabularySize, columns = topicsCount) { wordId, topicId ->
            val wordTopicProbability = wordTopicProbabilityMatrix[wordId, topicId]
            (0 until documentsCount).sumOf { documentId ->
                wordDocumentCountMatrix[wordId, documentId] * wordTopicProbability *
                        topicDocumentProbabilityMatrix[topicId, documentId] /
                        wordDocumentProbabilityMatrix[wordId, documentId]
            }
        }
        val totalWordsPerTopicMeans = (0 until topicsCount).map { topicId -> wordPerTopicMeans.col(topicId).sum() }

        val topicWordsPerDocumentMeans = matrix(rows = documentsCount, columns = topicsCount) { documentId, topicId ->
            val topicDocumentProbability = topicDocumentProbabilityMatrix[topicId, documentId]
            (0 until vocabularySize).sumOf { wordId ->
                wordDocumentCountMatrix[wordId, documentId] * wordTopicProbabilityMatrix[wordId, topicId] *
                        topicDocumentProbability /
                        wordDocumentProbabilityMatrix[wordId, documentId]
            }
        }
        val totalWordsPerDocumentMeans =
            (0 until documentsCount).map { documentId -> topicWordsPerDocumentMeans.row(documentId).sum() }

        wordTopicProbabilityMatrix.mapIndexed { wordId, topicId, _ ->
            wordPerTopicMeans[wordId, topicId] / totalWordsPerTopicMeans[topicId]
        }
        topicDocumentProbabilityMatrix.mapIndexed { topicId, documentId, _ ->
            topicWordsPerDocumentMeans[documentId, topicId] / totalWordsPerDocumentMeans[documentId]
        }
    }

    companion object {
        private val log by logger()
    }
}

