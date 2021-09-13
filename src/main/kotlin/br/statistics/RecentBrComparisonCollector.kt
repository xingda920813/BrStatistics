package br.statistics

import java.util.concurrent.TimeUnit

private class RecentKaidan2Stat(contributor: String, private val brCount: Int, weeks: Long) : Record<RecentKaidan2Stat> {

    private val density = brCount.toDouble() / weeks

    override fun compareTo(other: RecentKaidan2Stat) = other.brCount.compareTo(brCount)

    override val data = listOf(contributor, brCount.toString(), String.format("%.2f", density))
}

class RecentBrComparisonCollector(private val recentPeriod: Long) : DataCollector {

    private val weeks = TimeUnit.DAYS.convert(recentPeriod, TimeUnit.SECONDS) / 7
    private val kaidan1Stat = HashMap<String, Int>()

    override val tableHeaders = listOf("Contributor", "BrCount", "Density")

    override fun shouldContinueSearchGitLog(date: Long) = now - date <= recentPeriod

    override fun onInterestedRecordFound(contributor: String, date: Long) {
        val oldBrCount = kaidan1Stat[contributor]
        kaidan1Stat[contributor] = if (oldBrCount != null) oldBrCount + 1 else 1
    }

    override fun postProcess(): List<Record<*>> {
        val recentKaidan2Stat = ArrayList<RecentKaidan2Stat>()
        for ((contributor, brCount) in kaidan1Stat) {
            recentKaidan2Stat.add(RecentKaidan2Stat(contributor, brCount, weeks))
        }
        recentKaidan2Stat.sort()
        return recentKaidan2Stat
    }
}
