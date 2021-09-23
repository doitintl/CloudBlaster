package com.doitintl.blaster.test

import com.doitintl.blaster.test.tests.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis

import kotlin.reflect.KClass
import kotlin.system.exitProcess


private fun runAsync(
    classes: List<KClass<out TestBase>>, project: String
): Pair<MutableList<String>, MutableList<String>> {
    val successes = mutableListOf<String>()
    val failures = mutableListOf<String>()
    runBlocking {
        classes.forEach { `class` ->
            launch(Dispatchers.IO) {
                val t: TestBase = `class`.constructors.first().call(project)
                val result = t.test()
                if (result) {
                    successes.add(`class`.simpleName!!)
                } else {
                    failures.add(`class`.simpleName!!)
                }
            }
        }
    }
    return Pair(successes, failures)

}

fun main(vararg args: String) {
    val start=currentTimeMillis()
        if (args.isEmpty()) {
            println("Must provide project id as first arg")
            exitProcess(1)
        }
        val project = args[0]
        println("Project $project")
        val classes: List<KClass<out TestBase>> = listOf(
            GKETest::class,
            CloudRunTest::class,
            GAEServiceTest::class,
            BucketTest::class,
            PubSubTest::class,
            GCETest::class,
        )

        val (successes, failures) = runAsync(classes, project)
        val elapsedTimeSec= (currentTimeMillis()-start)/1000
         println("Elapsed $elapsedTimeSec s")
        if (failures.isNotEmpty()) {
            println("Done with ${failures.size} failures: ${failures.joinToString(",")}")
            exitProcess(1)
        } else {
            println("Done. Success in all ${successes.size} tests")
        }

}