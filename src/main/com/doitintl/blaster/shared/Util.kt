package com.doitintl.blaster.shared

import java.io.File
import java.util.concurrent.TimeUnit


fun noComment(allLines: List<String>): List<String> {
    return allLines.filter { l -> !l.trim().startsWith("#") }
}

fun noComment(content: String): String {
    val allLines = content.split("\n")
    val lines = noComment(allLines)
    return lines.joinToString("\n")
}


fun randomString(length: Int = 6): String {
    fun rand(chars: String, len: Int): String {
        return chars.toList().map { it }.shuffled().subList(0, len).joinToString("")
    }

    val vowels = "aeiouy".toList().joinToString("")
    val consonants = ('a'..'z').toList().filter { !vowels.contains(it) }.joinToString("")
    val randVowels = rand(vowels, length / 2)
    val randCons = rand(consonants, length / 2)
    return randVowels.zip(randCons) { a, b -> "$a$b" }.joinToString("")


}

fun runCommand(command: String, workingDirectory: File? = null) {
    val workingDir = workingDirectory ?: File(System.getProperty("user.dir"))

    val parts = command.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir).inheritIO()
        .start()

    proc.waitFor(60, TimeUnit.MINUTES)
    val exit = proc.exitValue()
    if (exit != 0) {
        throw RuntimeException(exit.toString())
    }

}