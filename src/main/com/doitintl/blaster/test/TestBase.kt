package com.doitintl.blaster.test

import com.doitintl.blaster.lister.LIST_THESE
import com.doitintl.blaster.lister.REGEX
import com.doitintl.blaster.shared.*
import com.doitintl.blaster.shared.Constants.COMMENT_READY_TO_DELETE
import com.doitintl.blaster.shared.Constants.LIST_FILTER_YAML
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.lang.System.currentTimeMillis
import java.util.*
import kotlin.system.exitProcess
import com.doitintl.blaster.deleter.main as deleter
import com.doitintl.blaster.lister.main as lister

abstract class TestBase(val project: String, private val sfx: String = randomString(8)) {


    /**
     * Create the assets that you are going to test.
     * @return a list that can have either
     *  - if secondaryAssetsExpected() is false, use a full path (in the pattern as given by AssetTypeDeleter.pathPatterns for that asset type)
     *  This is necessary when the local name is reused to create other secondary assets
     *  like the containers used by GAE.
     *  - if secondaryAssetsExpected() is false, use a local name (e.g., instance name, bucket name; not the full ID with path).
     *  This works in most cases
     */
    abstract fun createAssets(sfx: String, project: String): List<String>

    /**
     * @return list of asset types tested by this test, chosen from list-filter.yaml
     */
    abstract fun assetTypeIds(): List<String>

    init {
        println("Suffix $sfx for ${this::class.simpleName}")
        try {
            assert(false)
            System.err.println("Must enable assertions")
            exitProcess(1)
        } catch (ae: AssertionError) {
            // Asseertions are enabled, so we can continue
        }
    }

    fun test(): Boolean {
        return try {
            val assets = creationPhase(sfx, project)
            val (tempAssetToDeleteFile, tempFilterFile) = listAssetsWithFilter(sfx, project, assets)
            val content = tempAssetToDeleteFile.readText()
            //Put COMMENT_READY_TO_DELETE at end, and in upper case, to test a slightly unusual but supported case
            FileWriter(tempAssetToDeleteFile).use { fw ->
                fw.write(
                    content + "\n" + COMMENT_READY_TO_DELETE.toUpperCase()
                )
            }

            deletionPhase(tempAssetToDeleteFile, tempFilterFile, assets)
            println("Success ${this::class.simpleName}")
            true
        } catch (th: Throwable) {
            System.err.println("Error in ${this::class.simpleName}: ${th.stackTraceToString()}")
            false
        }
    }

    fun assetName(type: String): String {
        return "blastertest-$type-$sfx"
    }

    private fun creationPhase(sfx: String, project: String): List<String> {
        val assets = createAssets(sfx, project)
        validateAssetIdentifiers(assets)
        val notAll = { allAssets: String, assets_: List<String> -> !assets_.all { asset -> allAssets.contains(asset) } }

        waitOnUnfilteredOutput(notAll, assets)
        return assets
    }

    private fun validateAssetIdentifiers(assets: List<String>) {
        assert(assets.none { a -> a.contains("{") }) { "Should not have braces in $assets" }
        assert(assets.none { it.toLowerCase() != it }) { "Should not have uppercase in $assets" }
        val predicate: (String) -> Boolean = { a -> a.contains("/") }

        if (this.secondaryAssetsExpected()) {
            assert(assets.all(predicate)) { "Use full path when secondary assets expected: $assets" }
        } else {
            assert(assets.none(predicate)) { "Use local path when secondary assets not expected: $assets" }
        }
    }


    private fun deletionPhase(tempAssetToDeleteFile: File, tempFilterFile: File, assets: List<String>) {
        deleter(
            arrayOf(
                "--assets-to-delete-file",
                tempAssetToDeleteFile.absolutePath,
                "--filter-file",
                tempFilterFile.absolutePath
            )
        )
        val some =
            { fullListStr: String, delThese: List<String> -> !delThese.none { asset -> fullListStr.contains(asset) } }
        waitOnUnfilteredOutput(some, assets)

        val allAssets = listAllAssetsUnfiltered()
        if (!this.secondaryAssetsExpected()) {
            assert(!allAssets.contains(sfx)) {
                val linesWithSfx = allAssets.split("\n").filter { it.contains(sfx) }.joinToString("\n")
                "Found suffix $sfx in unfiltered output:\n$linesWithSfx"
            }
        }
    }

    private fun listAllAssetsUnfiltered(): String {
        val outputTempFile = createTempFile("all-assets$sfx", ".txt")
        outputTempFile.deleteOnExit()
        lister(arrayOf("--project", project, "--output-file", outputTempFile.absolutePath, "--no-filter"))
        return outputTempFile.readText()
    }


    private fun listAssetsWithFilter(sfx: String, project: String, expected: List<String>): Pair<File, File> {
        val tempFilterFilePath = newFilterYaml(sfx)
        val tempAssetsToDeleteFile = createTempFile("${Constants.ASSET_LIST_FILE}-$sfx", ".txt")
        tempAssetsToDeleteFile.deleteOnExit()
        lister(
            arrayOf(
                "--project",
                project,
                "--output-file",
                tempAssetsToDeleteFile.absolutePath,
                "--filter-file",
                tempFilterFilePath
            )
        )
        val outputRaw = tempAssetsToDeleteFile.readText()
        val output = noComment(outputRaw)
        assert(expected.all { output.contains(it) }) { "expected $expected \nbut output $output" }
        val filteredLines = output.split("\n").filter { it.isNotBlank() }

        assert(expected.size == filteredLines.size) { "expected $expected\noutput $output" }
        return Pair(tempAssetsToDeleteFile, File(tempFilterFilePath))
    }


    private fun waitOnUnfilteredOutput(
        waitCondition: (String, List<String>) -> Boolean,
        expected: List<String>,
    ) {

        val currentTime = currentTimeMillis()
        val target = currentTime + waitTimeMillis()

        var counter = 0
        do {
            val allAssets = listAllAssetsUnfiltered()
            if (counter % 4 == 0 && counter > 0) {
                println("Waiting in ${this::class.simpleName}: $counter")
            }
            counter++
            Thread.sleep(1000)//Must wait to avoid exceeding Asset Service quota
        } while (currentTimeMillis() < target && waitCondition(allAssets, expected))

        if (currentTimeMillis() > target) {
            throw TimeoutException("Timed out")
        }
    }

    /**
     * Timeout on waiting after an object is created/deleted before this is reflected
     * by the results of the Asset service. May take longer for GKE, for example.
     * But: Note that asset creation in the test blocks, so after asset creation,
     * the asset exists. The only question is whether the Asset Service shows it.
     * Deletion, however, does not block, so the waitTimeMillis is partially
     * a wait for deletion to complete.
     */
    open fun waitTimeMillis(): Long {
        val twoMin = 1000L * 60 * 10
        return twoMin
    }


    private fun newFilterYaml(sfx: String): String {
        var counter = 0
        FileInputStream(LIST_FILTER_YAML).use { `in` ->
            for (o in Yaml().loadAll(`in`)) {

                val filtersFromYaml = o as Map<String, Map<String, Any>>//The inner Map is Boolean|String
                val filtersFromYamlOut = TreeMap<String, Map<String, Any>>()
                for (assetTypeId: String in filtersFromYaml.keys) {
                    verifyAssetTypeIds()
                    val value: Map<String, Any> =
                        if (assetTypeIds().contains(assetTypeId)) {
                            mapOf(REGEX to ".*$sfx.*", LIST_THESE to true)
                        } else {
                            mapOf(REGEX to ".*", LIST_THESE to false)
                        }
                    filtersFromYamlOut[assetTypeId] = value

                }

                counter++
                if (counter > 1) {
                    throw IllegalConfigException("Only 1 object allowed in root of list-filter.yaml")
                }

                return writeTempFilterYaml(sfx, filtersFromYamlOut)
            }
        }
        throw        IllegalCodePathException("Should not get here")
    }

    private fun verifyAssetTypeIds() {

        val knownIds = mutableListOf<String>()
        FileInputStream(LIST_FILTER_YAML).use { `in` ->

            for (o in Yaml().loadAll(`in`)) {
                val filtersFromYaml = o as Map<String, Map<String, Any>>//The  Any is Boolean|String
                for (assetTypeId in filtersFromYaml.keys) {
                    knownIds.add(assetTypeId)
                }
            }
        }

        assetTypeIds().forEach {
            assert(knownIds.contains(it)) { "$it unknown; see asset-types.properties for valid API identifiers" }
        }
    }

    //todo If secondary assets are expected, as in GAE, then garbage will be left after the test (containers for GAE)
    open fun secondaryAssetsExpected(): Boolean {
        return false
    }


    private fun writeTempFilterYaml(
        sfx: String,
        filtersFromYamlOut: TreeMap<String, Map<String, Any>>
    ): String {
        val baseFilename = LIST_FILTER_YAML.split(".")[0]
        val tempFilterYaml = createTempFile("$baseFilename-$sfx", ".yaml")
        tempFilterYaml.deleteOnExit()
        writeYaml(tempFilterYaml.absolutePath, filtersFromYamlOut)
        return tempFilterYaml.absolutePath
    }

    private fun writeYaml(filePath: String, newYaml: Map<String, Map<String, Any>>) {
        val dumperOptions = DumperOptions()
        dumperOptions.isPrettyFlow = true
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        dumperOptions.defaultScalarStyle = DumperOptions.ScalarStyle.DOUBLE_QUOTED
        val yaml = Yaml(dumperOptions)
        FileWriter(filePath).use { fw ->
            yaml.dump(newYaml, fw)
        }
    }
}
