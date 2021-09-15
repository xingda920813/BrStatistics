package br.statistics

interface Record<T> : Comparable<T> {

    val data: List<String>
}

interface DataCollector {

    val tableHeaders: List<String>

    fun shouldContinueSearchGitLog(date: Long): Boolean

    fun onInterestedRecordFound(contributor: String, date: Long)

    fun postProcess(): List<Record<*>>
}
