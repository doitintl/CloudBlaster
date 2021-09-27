package com.doitintl.blaster.test

import com.doitintl.blaster.lister.LIST_THESE
import com.doitintl.blaster.lister.REGEX
import com.doitintl.blaster.shared.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.shared.writeTempFilterYaml
import com.doitintl.blaster.test.tests.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass
import kotlin.system.exitProcess
import com.doitintl.blaster.lister.main as lister


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
    val start = currentTimeMillis()
    if (args.isEmpty()) {
        System.err.println("Must provide project as first arg")
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
    val elapsedTimeSec = (currentTimeMillis() - start) / 1000
    println("TestRunner total time: ${elapsedTimeSec}s")
    if (failures.isNotEmpty()) {
        System.err.println("Done with ${failures.size} failures: ${failures.joinToString(",")}")
        messageForCleanup(project)
        exitProcess(1)
    } else {
        println("TestRunner Done. Success in all ${successes.size} tests")
    }

}


fun messageForCleanup(project: String) {
    val unfiltered =
        { _: String?, _: List<String>?, _: String? ->
            mapOf(REGEX to ".*", LIST_THESE to true)
        }

    val tempFilterFilePath = writeTempFilterYaml(null, null, unfiltered)
    val tempAssetsToDeleteFile = createTempFile(ASSET_LIST_FILE, ".txt")
    tempAssetsToDeleteFile.deleteOnExit()

    lister(
        arrayOf(
            "--project",
            project,
            "--output-file",
            tempAssetsToDeleteFile.absolutePath,
            "--filter-file",
            tempFilterFilePath.absolutePath
        )
    )
    val found = tempAssetsToDeleteFile.readText()
    val lines = found.split("\n")
    val linesS = lines.subList(1, lines.lastIndex).joinToString("\n")
    System.err.println("\nTest failed: Check project $project for undeleted test assets. Assets found were:\n\n$linesS")
}
