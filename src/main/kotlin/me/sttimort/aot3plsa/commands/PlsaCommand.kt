package me.sttimort.aot3plsa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import me.sttimort.aot3plsa.*
import org.apache.commons.csv.CSVFormat
import java.io.File

class PlsaCommand : CliktCommand(name = "plsa") {
    private val corpusCsvFilePath: String by option("--corpus-csv").required()
    private val csvSeparatorParam: String by option("--csv-separator", "--csv-sep").default(",")
    private val targetColumnName: String by option("--target-col").required()

    private val topicsCount: Int by option("--topics").int().required()
    private val topNWordsInTopicCount: Int by option("--top-n").int().default(10)

    override fun run() {
        log.info { "Building corpus" }
        val corpus: Corpus = extractTexts(corpusCsvFile = getFileOrThrow(corpusCsvFilePath))
            .take(500)
            .let { Corpus.from(texts = it) }

        log.info { "Build corpus" }

        val model = buildFittedPlsaModel(corpus, topicsCount)

        model.topNWordsPerTopics(topNWordsInTopicCount).forEachIndexed { topicIndex, topWords ->
            println("top $topNWordsInTopicCount words in topic ${topicIndex + 1} -- $topWords")
        }
    }

    private fun extractTexts(corpusCsvFile: File): List<String> {
        val csvFormat = CSVFormat
            .newFormat(csvSeparatorParam.first())
            .withQuote('"')
            .withHeader()
            .withSystemRecordSeparator()

        return csvFormat
            .parse(corpusCsvFile.bufferedReader())
            .asSequence()
            .map { it.get(targetColumnName) }
            .toList()
    }

    companion object {
        private val log by logger()
    }
}