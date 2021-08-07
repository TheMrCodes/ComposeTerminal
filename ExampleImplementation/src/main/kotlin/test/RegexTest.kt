package test

import androidx.compose.runtime.remember

fun main() {
    val regex = Regex("\\u001b\\[[0-8]{0,8};?(3[0-9])?;?(4[0-9])?[mA-K]")


    val tests = listOf(
        "\u001b[0m",
        "\u001b[1m",
        "\u001b[2m",
        "\u001b[3m",
        "\u001b[4m",
        "\u001b[5m",
        "\u001b[6m",
        "\u001b[7m",
        "\u001b[8m",
        "\u001b[0;30m",
        "\u001b[0;30;40m",
        "\u001b[1;34;42m",
        "\u001b[4;34;42m",
        "\u001b[01;34m",
        "\u001b[01;31m",
    )
    for (t in tests)
        println(if (regex.matches(t)) "true" else "false")

    val line = "\u001B[0m\u001B[01;34mdownload\u001B[0m          \u001B[01;31mPianoPaddleServer-1.0-7.jar\u001B[0m  \u001B[01;34mvban\u001B[0m"
    val found = regex.findAll(line).toList().map { it.value }
    println(found.size)

}