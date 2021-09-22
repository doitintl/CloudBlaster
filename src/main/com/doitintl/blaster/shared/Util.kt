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
    return ('a'..'z').map { it }.shuffled().subList(0, length).joinToString("")
}

fun runCommand(command: String, workingDirectory: File? = null): String {
    val workingDir = workingDirectory ?: File(System.getProperty("user.dir"))

    val parts = command.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(60, TimeUnit.MINUTES)
    val ret = proc.inputStream.bufferedReader().readText()
    val exit = proc.exitValue()
    if (exit != 0) {
        throw RuntimeException(exit.toString())
    }
    return ret

}