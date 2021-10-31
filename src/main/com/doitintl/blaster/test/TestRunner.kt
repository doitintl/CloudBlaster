package com.doitintl.blaster.test

import com.doitintl.blaster.shared.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.test.tests.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass
import kotlin.system.exitProcess
import com.doitintl.blaster.lister.run as runLister


private fun runSync(
    classes: List<KClass<out TestBase>>, project: String
): Pair<List<String>, List<String>> {
    val successes = mutableListOf<String>()
    val failures = mutableListOf<String>()

    classes.forEach { `class` ->
        val t: TestBase = `class`.constructors.first().call(project)
        val result = t.test()
        if (result) {
            successes.add(`class`.simpleName!!)
        } else {
            failures.add(`class`.simpleName!!)
        }

    }
    return Pair(successes, failures)
}

private fun runAsync(
    classes: List<KClass<out TestBase>>, project: String
): Pair<List<String>, List<String>> {
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


fun messageForCleanup(project: String) {

    val tempAssetsToDeleteFile = createTempFile(ASSET_LIST_FILE, ".txt")
    tempAssetsToDeleteFile.deleteOnExit()

    runLister(
        arrayOf(
            "--project",
            project,
            "--output-file",
            tempAssetsToDeleteFile.absolutePath,
        )
    )
    val found = tempAssetsToDeleteFile.readText()
    val lines = found.split("\n")
    val linesS = lines.subList(1, lines.lastIndex).joinToString("\n")
    System.err.println("\nTest failed: Check project $project for undeleted test assets. Assets found were:\n\n$linesS")
}


fun main(vararg args: String) {
    val start = currentTimeMillis()
    if (args.isEmpty()) {
        System.err.println("Must provide project as first arg")
        exitProcess(1)
    }
    val project = args[0]
    println("Project $project")
    val classes: List<KClass<out TestBase>> = listOf(
        BucketTest::class,
        PubSubTest::class,
        GCETest::class,
        CloudFunctionTest::class,
        CloudRunTest::class,
        GAEServiceTest::class,
        GKETest::class,
    )

    val (successes, failures) = runAsync(classes, project)
    val elapsedTimeSec = (currentTimeMillis() - start) / 1000
    println("TestRunner total time: ${elapsedTimeSec}s")

    if (failures.isNotEmpty()) {
        System.err.println("Done with ${failures.size} failures: ${failures.joinToString(",")}")
        messageForCleanup(project)
        exitProcess(1)
    } else {
        println("TestRunner Done. Success in all ${successes.size} tests")
        exitProcess(0)
    }

}
