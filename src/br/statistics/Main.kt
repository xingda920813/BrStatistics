package br.statistics

import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

// git log --date raw
private const val PATH_FULL_LOG = "C:\\Users\\TODO\\Desktop\\full_log.txt"
private const val PATH_CONTRIBUTORS = "C:\\Users\\TODO\\Desktop\\contributors.txt"
private const val PATH_OUTPUT = "C:\\Users\\TODO\\Desktop\\output.csv"

// switch mode below
private val dataCollector = RecentBrComparisonCollector(TimeUnit.SECONDS.convert(180, TimeUnit.DAYS))
//private val dataCollector = TotalBrForAllContributorsCollector()

val now = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

fun main() {
    val contributors = Files
        .readAllLines(Paths.get(PATH_CONTRIBUTORS))
        .stream()
        .filter { !it.isNullOrEmpty() }
        .collect(Collectors.toSet())
    val lines = Files.readAllLines(Paths.get(PATH_FULL_LOG))
    var i = 0
    val size = lines.size
    while (i < size) {
        val line = lines[i]
        if (line.isNullOrEmpty()) {
            i++
            continue
        }
        if (line.startsWith("Author:")) {
            val interestedContributor = contributors
                .stream()
                .filter { contributor -> line.contains(contributor) }
                .findAny()
                .orElse(null)
            if (interestedContributor.isNullOrEmpty()) {
                i++
                continue
            }
            val nextLine = lines[i + 1]
            require(nextLine?.startsWith("Date:") == true)
            val date = getDate(nextLine)
            if (dataCollector.shouldContinueSearchGitLog(date)) {
                dataCollector.onInterestedRecordFound(interestedContributor, date)
            } else {
                break
            }
        }
        i++
    }
    val records = dataCollector.postProcess()
    FileWriter(PATH_OUTPUT).use { writer ->
        writer.write(dataCollector.tableHeaders.joinToString(","))
        writer.write("\n")
        for (record in records) {
            writer.write(record.data.joinToString(","))
            writer.write("\n")
        }
    }
}
