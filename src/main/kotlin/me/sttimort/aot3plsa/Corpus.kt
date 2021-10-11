package me.sttimort.aot3plsa

import com.londogard.nlp.stemmer.Stemmer
import com.londogard.nlp.stopwords.Stopwords
import com.londogard.nlp.utils.LanguageSupport
import smile.nlp.tokenizer.BreakIteratorTokenizer
import java.util.*

data class Corpus(
    val vocabulary: Vocabulary,
    val documentsCount: Int,
    private val wordDocumentInclusions: Map<Pair<Int, Int>, Int>,
) {
    companion object {
        private val builder = CorpusBuilder()

        fun from(texts: Collection<String>): Corpus = builder.build(texts)
    }

    fun countWordDocumentInclusions(wordIndex: Int, documentIndex: Int): Int =
        wordDocumentInclusions[Pair(wordIndex, documentIndex)] ?: 0

    data class Document(val id: Int, val words: List<String>)

    class Vocabulary {
        private val indexesToWords = mutableMapOf<Int, String>()
        private val wordsToIndexes = mutableMapOf<String, Int>()

        val size: Int get() = wordsToIndexes.size

        fun getWordIndex(word: String) = wordsToIndexes
            .computeIfAbsent(word) { wordsToIndexes.size }
            .also { index -> indexesToWords.computeIfAbsent(index) { word } }

        fun getWordByIndex(index: Int): String? = indexesToWords[index]
    }
}


private class CorpusBuilder {
    private val tokenizer = BreakIteratorTokenizer(Locale("RU"))
    private val nonCyrillicWordRegex = "^[^a-яА-Я]+|[«»]+$".toRegex()
    private val stopWords = Stopwords.stopwords(LanguageSupport.ru).plus(
        listOf(
            "из-за", "когда-то", "во-первых", "во-вторых", "в-третьих", "где-нибудь", "который", "которая", "однако",
            "это",
        )
    )
    private val stemmer = Stemmer(LanguageSupport.ru)

    fun build(texts: Collection<String>): Corpus {
        val documents = texts
            .asSequence()
            .map(tokenizer::split)
            .map { wordsArray ->
                wordsArray
                    .asSequence()
                    .filter { !it.matches(nonCyrillicWordRegex) }
                    .map { it.lowercase() }
                    .filter { !stopWords.contains(it) && it.length > 1 }
                    .map(stemmer::stem)
                    .toList()
            }
            .mapIndexed { index, words -> Corpus.Document(id = index, words) }
            .toList()

        val vocabulary = Corpus.Vocabulary()
        val wordDocumentInclusions = mutableMapOf<Pair<Int, Int>, Int>()
        documents.forEach { document ->
            document.words.forEach { word ->
                val wordId = vocabulary.getWordIndex(word)
                wordDocumentInclusions.merge(Pair(wordId, document.id), 1) { old, _ -> old + 1 }
            }
        }

        return Corpus(
            vocabulary = vocabulary,
            documentsCount = documents.size,
            wordDocumentInclusions = wordDocumentInclusions,
        )
    }
}

