package com.doitintl.blaster.test

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


fun randomString(length: Int): String {
    return ('a'..'z').map { it }.shuffled().subList(0, length).joinToString("")
}


fun runCommand(command: String, workingDir_: File? = null): String {
    val workingDir = workingDir_ ?: File(System.getProperty("user.dir"))
    return try {
        val parts = command.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        e.toString()
    }
}