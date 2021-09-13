package br.statistics

import java.util.concurrent.TimeUnit

class Kaidan2Stat(private val contributor: String, private val brCount: Int) : Comparable<Kaidan2Stat> {

    private val weeks = TimeUnit.DAYS.convert(recentPeriod, TimeUnit.SECONDS) / 7
    private val density = brCount.toDouble() / weeks

    override fun compareTo(other: Kaidan2Stat): Int {
        return other.brCount.compareTo(brCount)
    }

    override fun toString(): String {
        return contributor + ',' + brCount + ',' + String.format("%.2f", density)
    }
}
