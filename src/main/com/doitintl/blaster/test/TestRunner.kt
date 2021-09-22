package com.doitintl.blaster.test

import com.doitintl.blaster.test.tests.*
import kotlin.system.exitProcess


fun main(vararg args: String) {
    if (args.isEmpty()) {
        println("Must provide project id")
        exitProcess(1)
    }
    val project = args[0]
    println("Project $project")
    val results = ArrayList<String>()
    val cls1 = GCETest::class

    val t: TestBase = cls1.constructors.first().call(project)
    results.add(t.test())
//todo do this for all test classes, then parallelize

    //results.add(GCETest(project).test())
    results.add(BucketTest(project).test())
    results.add(PubSubTest(project).test())
    results.add(CloudRunTest(project).test())
    results.add(GAETest(project).test())
    results.add(LogMetricTest(project).test())
    results.add(TrainingPipelineTest(project).test())
    if (!results.all { it.isBlank() }) {
        println(results.joinToString("\n"))
        exitProcess(1)
    } else {
        println("Success in test")

    }
}
