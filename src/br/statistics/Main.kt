package br.statistics

import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val RECENT_MODE = true

class Kaidan2Stat(private val mContributor: String, private val mBrCount: Int, joinTime: Long) : Comparable<Kaidan2Stat> {

    private val mJoinWeeks = (TimeUnit.DAYS.convert(NOW - joinTime, TimeUnit.SECONDS) / 7).toInt()
    private val mDensity = mBrCount.toDouble() / mJoinWeeks

    override fun compareTo(other: Kaidan2Stat): Int {
        return if (RECENT_MODE) {
            other.mBrCount.compareTo(mBrCount)
        } else {
            other.mDensity.compareTo(mDensity)
        }
    }

    override fun toString(): String {
        return mContributor + ',' + mBrCount + ',' + mJoinWeeks + ',' + String.format("%.2f", mDensity)
    }

    companion object {

        val NOW = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    }
}

interface Main {

    companion object {

        // git log --date raw
        private const val PATH_FULL_LOG = "C:\\Users\\TODO\\Desktop\\full_log.txt"
        private const val PATH_CONTRIBUTORS = "C:\\Users\\TODO\\Desktop\\contributors.txt"
        private const val PATH_OUTPUT = "C:\\Users\\TODO\\Desktop\\output.csv"
        private val recentPeriod = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)

        @JvmStatic
        fun main(args: Array<String>) {
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
                    if (!RECENT_MODE || Kaidan2Stat.NOW - date <= recentPeriod) {
                        kaidan1Stat.computeIfAbsent(interestedContributor) { ArrayList() }.add(date)
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

        private fun getDate(line: String): Long {
            val start = "Date:".length
            var end = line.lastIndexOf('+')
            if (end == -1) end = line.lastIndexOf('-')
            val pureDateString = line.substring(start, end).trim()
            return pureDateString.toLong()
        }
    }
}
