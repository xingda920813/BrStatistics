package br.statistics

import java.util.concurrent.TimeUnit

class Kaidan2Stat(private val contributor: String, private val brCount: Int, joinTime: Long) : Comparable<Kaidan2Stat> {

    private val joinWeeks = (TimeUnit.DAYS.convert(nowInSeconds - joinTime, TimeUnit.SECONDS) / 7).toInt()
    private val density = brCount.toDouble() / joinWeeks

    override fun compareTo(other: Kaidan2Stat): Int {
        return if (RECENT_MODE) {
            other.brCount.compareTo(brCount)
        } else {
            other.density.compareTo(density)
        }
    }

    override fun toString(): String {
        return contributor + ',' + brCount + ',' + joinWeeks + ',' + String.format("%.2f", density)
    }
}
