package br.statistics

fun getDate(line: String): Long {
    val start = "Date:".length
    var end = line.lastIndexOf('+')
    if (end == -1) end = line.lastIndexOf('-')
    val pureDateString = line.substring(start, end).trim()
    return pureDateString.toLong()
}
