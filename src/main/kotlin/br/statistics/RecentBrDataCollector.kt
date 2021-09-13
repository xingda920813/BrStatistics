package br.statistics

import java.util.concurrent.TimeUnit

private class Kaidan2Stat(contributor: String, private val brCount: Int, recentPeriod: Long) : Record<Kaidan2Stat> {

    private val weeks = TimeUnit.DAYS.convert(recentPeriod, TimeUnit.SECONDS) / 7
    private val density = brCount.toDouble() / weeks

    override fun compareTo(other: Kaidan2Stat): Int {
        return other.brCount.compareTo(brCount)
    }

    override val data = listOf(contributor, brCount.toString(), String.format("%.2f", density))
}

class RecentBrDataCollector(private val recentPeriod: Long) : DataCollector {

    private val kaidan1Stat = HashMap<String, Int>()

    override val tableHeaders = listOf("Contributor", "BrCount", "Density")

    override fun shouldContinueSearchGitLog(date: Long) = now - date <= recentPeriod

    override fun onInterestedRecordFound(contributor: String, date: Long) {
        val oldBrCount = kaidan1Stat[contributor]
        kaidan1Stat[contributor] = if (oldBrCount != null) oldBrCount + 1 else 1
    }

    override fun postProcess(): List<Record<*>> {
        val kaidan2Stat = ArrayList<Kaidan2Stat>()
        for ((contributor, brCount) in kaidan1Stat) {
            kaidan2Stat.add(Kaidan2Stat(contributor, brCount, recentPeriod))
        }
        kaidan2Stat.sort()
        return kaidan2Stat
    }
}
