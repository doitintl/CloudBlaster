package com.doitintl.blaster.test

import com.doitintl.blaster.test.tests.BucketTest
import com.doitintl.blaster.test.tests.InstanceTest


fun main(vararg args: String) {
    if (args.isEmpty()) {
        println("Must provide project id")
        System.exit(1)
    }
    val project = args[0]
    println("Project $project")

    BucketTest(project).createListDeleteTest(project)
    InstanceTest(project).createListDeleteTest(project)

    println("DONE")
}