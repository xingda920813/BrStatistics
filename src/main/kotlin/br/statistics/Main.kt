package br.statistics

import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// git log --date raw
private const val PATH_FULL_LOG = "C:\\Users\\TODO\\Desktop\\full_log.txt"
private const val PATH_CONTRIBUTORS = "C:\\Users\\TODO\\Desktop\\contributors.txt"
private const val PATH_OUTPUT = "C:\\Users\\TODO\\Desktop\\output.csv"

const val RECENT_MODE = true
private val recentPeriod = TimeUnit.SECONDS.convert(60, TimeUnit.DAYS)

val nowInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

fun main() {
    val kaidan1Stat = HashMap<String, ArrayList<Long>>()
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
            if (!RECENT_MODE || nowInSeconds - date <= recentPeriod) {
                kaidan1Stat.computeIfAbsent(interestedContributor) { ArrayList() }.add(date)
            } else {
                break
            }
        }
        i++
    }
    val kaidan2Stat = ArrayList<Kaidan2Stat>()
    for ((contributor, kiroku) in kaidan1Stat) {
        kiroku.sort()
        kaidan2Stat.add(Kaidan2Stat(contributor, kiroku.size, kiroku[0]))
    }
    kaidan2Stat.sort()
    FileWriter(PATH_OUTPUT).use { writer ->
        writer.write("Contributor,BrCount,JoinWeeks,Density")
        writer.write("\n")
        for (stat in kaidan2Stat) {
            writer.write(stat.toString())
            writer.write("\n")
        }
    }
}
