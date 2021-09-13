package br.statistics

import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy-MM")

private class TotalKaidan2Stat(private val calendar: Calendar, brCount: Int, weeks: Long) : Record<TotalKaidan2Stat> {

    private val date = dateFormat.format(Date(calendar.timeInMillis))
    private val density = brCount.toDouble() / weeks

    override fun compareTo(other: TotalKaidan2Stat): Int {
        return calendar.timeInMillis.compareTo(other.calendar.timeInMillis)
    }

    override val data = listOf(date, brCount.toString(), String.format("%.2f", density))
}

class TotalBrForAllContributorsCollector : DataCollector {

    private val weeks = ChronoUnit.MONTHS.duration.toDays() / 7
    private val kaidan1Stat = HashMap<Calendar, Int>()

    override val tableHeaders = listOf("Date", "BrCount", "Density")

    override fun shouldContinueSearchGitLog(date: Long) = true

    override fun onInterestedRecordFound(contributor: String, date: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date * 1000
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val oldBrCount = kaidan1Stat[calendar]
        kaidan1Stat[calendar] = if (oldBrCount != null) oldBrCount + 1 else 1
    }

    override fun postProcess(): List<Record<*>> {
        val totalKaidan2Stat = ArrayList<TotalKaidan2Stat>()
        for ((calendar, brCount) in kaidan1Stat) {
            totalKaidan2Stat.add(TotalKaidan2Stat(calendar, brCount, weeks))
        }
        totalKaidan2Stat.sort()
        return totalKaidan2Stat
    }
}
