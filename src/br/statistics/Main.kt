package br.statistics

import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.TreeSet
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Kaidan2Stat(private val mContributor: String, private val mBrCount: Int, joinTime: Long) : Comparable<Kaidan2Stat> {

    private val mJoinWeeks = (TimeUnit.DAYS.convert(NOW - joinTime, TimeUnit.SECONDS) / 7).toInt()
    private val mDensity = mBrCount.toDouble() / mJoinWeeks

    override fun compareTo(other: Kaidan2Stat): Int {
        return other.mDensity.compareTo(mDensity)
    }

    override fun toString(): String {
        return mContributor + ',' + mBrCount + ',' + mJoinWeeks + ',' + String.format("%.2f", mDensity)
    }

    companion object {

        val NOW = System.currentTimeMillis() / 1000
    }
}

interface Main {

    companion object {

        // git log --date raw
        private val PATH_FULL_LOG = TODO() as String
        private val PATH_CONTRIBUTORS = TODO() as String
        private val PATH_OUTPUT = TODO() as String

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
                    kaidan1Stat.computeIfAbsent(interestedContributor) { ArrayList() }.add(date)
                }
                i++
            }
            val kaidan2Stat = TreeSet<Kaidan2Stat>()
            for ((contributor, kiroku) in kaidan1Stat) {
                kiroku.sort()
                kaidan2Stat.add(Kaidan2Stat(contributor, kiroku.size, kiroku[0]))
            }
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
