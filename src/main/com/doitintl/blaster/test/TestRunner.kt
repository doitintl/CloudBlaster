package com.doitintl.blaster.test

import com.doitintl.blaster.test.tests.BucketTest
import com.doitintl.blaster.test.tests.GCETest
import com.doitintl.blaster.test.tests.PubSubTest
import kotlin.system.exitProcess


fun main(vararg args: String) {
    if (args.isEmpty()) {
        println("Must provide project id")
        exitProcess(1)
    }
    val project = args[0]
    println("Project $project")
    val results = ArrayList<String>()
    results.add(GCETest(project).test())
    results.add(BucketTest(project).test())
    results.add(PubSubTest(project).test())
    if (!results.all { it.isBlank() }) {
        println(results.joinToString("\n"))
        exitProcess(1)
    } else {
        println("Success in test")

    }
}