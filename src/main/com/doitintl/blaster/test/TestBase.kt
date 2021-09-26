package com.doitintl.blaster.test

import com.doitintl.blaster.lister.LIST_THESE
import com.doitintl.blaster.lister.REGEX
import com.doitintl.blaster.shared.*
import com.doitintl.blaster.shared.Constants.COMMENT_READY_TO_DELETE
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LIST_FILTER_YAML
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
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
        val notAll = { allAssets: String, assets_: List<String> ->
            !assets_.all { asset ->
                fullListingContainsAsset(
                    allAssets,
                    asset
                )
            }
        }

        waitOnUnfilteredOutput(notAll, "creation", assets)
        return assets
    }

    private fun validateAssetIdentifiers(assets: List<String>) {
        assert(assets.none { a -> a.contains("{") }) { "Should not have braces in $assets" }
        assert(assets.none { it.toLowerCase() != it }) { "Should not have uppercase in $assets" }
        val predicate: (String) -> Boolean = { a -> a.contains("/") }

        if (this.identifierIsFullPath()) {
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
            { fullListStr: String, delThese: List<String> ->
                !delThese.none { asset ->
                    fullListingContainsAsset(
                        fullListStr,
                        asset
                    )
                }
            }
        waitOnUnfilteredOutput(some, "deletion", assets)

        val allAssets = listAllAssetsUnfiltered()
        if (!this.identifierIsFullPath()) {
            assert(!allAssets.contains(sfx)) {
                val linesWithSfx = allAssets.split("\n").filter { it.contains(sfx) }.joinToString("\n")
                "Found suffix $sfx in unfiltered output:\n$linesWithSfx"
            }
        }
    }

    /**
     * If we are using full paths, check that the exact path is listed. Otherwise, just
     * look for the string.
     */
    private fun fullListingContainsAsset(fullList: String, asset: String): Boolean {
        return if (!identifierIsFullPath()) {
            fullList.contains(asset)
        } else {
            val lines = fullList.split("\n")
            lines.any { line -> asset == line }
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
        waitCondition: (String, List<String>) -> Boolean, phase: String, expected: List<String>,
    ) {

        val start = currentTimeMillis()
        val timeout = start + waitTimeMillis()


        while (true) {
            val allAssets = listAllAssetsUnfiltered()
            println("Waiting for $phase in ${this::class.simpleName}: ${(currentTimeMillis() - start) / 1000}s passed")
            if (currentTimeMillis() > timeout || !waitCondition(allAssets, expected)) {
                break
            }
            Thread.sleep(2000)//Must wait to avoid exceeding Asset Service quota
        }

        if (currentTimeMillis() >= timeout) {
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
        val twoMin = 2 * 60 * 1000L
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


    // todo use fullpath as identifier for ALL asset types, not just where we have to.
    //  This will require developing the code to do that consistenly and easily.
    //
    // todo We use identifierIsFullPath == true where secondary assets are expected (e.g., GAE Service, GKE Cluster),
    // and then garbage may be left after the test (containers in the case GAE). Clean this garbage up.
    // (The garbage is not special to Cloud Blaster or this test -- it will always happen when
    // generating such assets.)

    /**
     * Use full path as identifier if secondary assets are expected (e.g., GAE Service which generates containers),
     * because in these cases,the asset name by itself is not enough to identify the asset
     */

    open fun identifierIsFullPath(): Boolean {
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


    fun pathForAsset(pattern: String, project: String, name: String, location: String? = null): String {

        val replaced = pattern.replace("{${PROJECT.toUpperCase()}}", project).replace("{${ID.toUpperCase()}}", name)
        val ret = if (location == null) {
            replaced
        } else {
            replaced.replace("{${LOCATION.toUpperCase()}}", location)
        }
        assert(ret != pattern)
        assert(!ret.contains("{"))
        return ret
    }
}
