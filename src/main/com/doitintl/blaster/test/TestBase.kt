package com.doitintl.blaster.test

import com.doitintl.blaster.lister.AssetTypeMap
import com.doitintl.blaster.shared.*
import com.doitintl.blaster.shared.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.shared.Constants.COMMENT_READY_TO_DELETE
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LIST_FILTER_YAML
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.lang.System.currentTimeMillis
import java.util.*
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import com.doitintl.blaster.deleter.run as runDeleter
import com.doitintl.blaster.lister.run as runLister

abstract class TestBase(val project: String, private val sfx: String = randomString(8)) {
    private val testInput = "test-input"


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
     * @return list of asset types tested by this test, chosen from list-filter.yaml or asset-types.properties
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
            validateAssetTypeIds()
            val assets = creationPhase(sfx, project)
            deletionPhase(assets)
            println("Success ${this::class.simpleName}")
            true
        } catch (th: Throwable) {
            System.err.println("Failure in ${this::class.simpleName}: ${th.stackTraceToString()}")
            finalNonblockingCleanup()

            false
        }
    }

    private fun deletionPhase(assets: List<String>) {
        val (tempAssetToDeleteFile, tempFilterFile) = listAssetsWithFilter(sfx, project, assets)
        addDeletionEnablement(tempAssetToDeleteFile)

        runDeleter(
            arrayOf(
                "--assets-to-delete-file",
                tempAssetToDeleteFile.absolutePath,

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

        waitForAssets(some, "deletion", assets)


        // We assert that there are no assets with this sfx string in them.
        // If we are using fullPath, then we could assert that that there are no assets with this full path in them.
        // However, we already do that in waitOnUnfilteredOutput (line above) so it is not useful.
        if (!identifierIsFullPath()) {
            val allAssets = listAllAssetsForAllSupportedAssetTypes()
            assert(!allAssets.contains(sfx)) {
                val foundLines = allAssets.split("\n").filter { it.contains(sfx) }.joinToString("\n")
                "Found suffix $sfx in unfiltered output:\n$foundLines"
            }
        }
    }

    private fun addDeletionEnablement(tempAssetToDeleteFile: File) {
        val content = tempAssetToDeleteFile.readText()
        //Put COMMENT_READY_TO_DELETE at end, and in upper case, to test a slightly unusual but supported case
        FileWriter(tempAssetToDeleteFile).use { fw ->
            fw.write(
                content + "\n" + COMMENT_READY_TO_DELETE.uppercase(Locale.getDefault())
            )
        }
    }

    private fun finalNonblockingCleanup() {
        System.err.println("finalNonblockingCleanup: $sfx")
        val (tempAssetToDeleteFile, tempFilterFile) = listAssetsWithFilter(sfx, project)
        addDeletionEnablement(tempAssetToDeleteFile)

        runDeleter(
            arrayOf(
                "--assets-to-delete-file",
                tempAssetToDeleteFile.absolutePath,
            )
        )
    }

    fun assetName(assetTypeShortId: String): String {
        assert(assetTypeShortId == assetTypeShortId.lowercase(Locale.getDefault())) { "Asset ID must be lower-case: $assetTypeShortId" }

        return "blastertest${assetNameSeparator()}${assetTypeShortId}${assetNameSeparator()}$sfx"
    }

    /**
     * A char that is legal for this asset type. Used to build asset name in the form
     * blastertest-<ASSET-TYPE>-<RANDOM-STRING>
     * Override this method where the default value would not work
     * (For example, in Cloud Functions, where the asset name must also be the name of a function in Python).
     */
    open fun assetNameSeparator(): String {
        return "-"
    }

    private fun creationPhase(sfx: String, project: String): List<String> {
        val assets = createAssets(sfx, project)
        assertAssetIdStructure(assets)
        val notAll = { allAssets: String, assets_: List<String> ->
            !assets_.all { asset ->
                fullListingContainsAsset(
                    allAssets,
                    asset
                )
            }
        }

        waitForAssets(notAll, "creation", assets)
        return assets
    }

    /**
     * This is an internal assertion (not a check of external input), intended to handle the
     * complexity of the fact that we use either  full-path or local-name.
     * We check that the usage in fact matches the definition in the Deleter class.
     */
    private fun assertAssetIdStructure(assets: List<String>) {
        assert(assets.none { a -> a.contains("{") }) { "Should not have braces in $assets" }
        assert(assets.none { it.lowercase(Locale.getDefault()) != it }) { "Should not have uppercase in $assets" }
        val predicate: (String) -> Boolean = { a -> a.contains("/") }

        if (this.identifierIsFullPath()) {
            assert(assets.all(predicate)) { "Use full path when secondary assets expected: $assets" }
        } else {
            assert(assets.none(predicate)) { "Use local path when secondary assets not expected: $assets" }
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

    /**
     * We could instead list here ALL assets (see --no-filter option).
     * That would achieve test coverage on that option,
     * We don't do that simply to speed up the tests, as there can be very many assets across all GCP asset types.
     */
    private fun listAllAssetsForAllSupportedAssetTypes(): String {
        val outputTempFile = createTempFile("all-supported-assets$sfx", ".txt")
        outputTempFile.deleteOnExit()
        val filterTempFile = writeTempFilterYaml(AssetTypeMap().assetTypeIds(), "")
        runLister(
            arrayOf(
                "--project",
                project,
                "--output-file",
                outputTempFile.absolutePath,
                "--filter-file",
                filterTempFile.absolutePath
            )
        )
        return outputTempFile.readText()
    }


    private fun listAssetsWithFilter(sfx: String, project: String, expected: List<String>? = null): Pair<File, File> {

        val tempFilterFilePath = writeTempFilterYaml(assetTypeIds(), sfx)
        val tempAssetsToDeleteFile = createTempFile("$ASSET_LIST_FILE-$sfx", ".txt")
        tempAssetsToDeleteFile.deleteOnExit()
        runLister(
            arrayOf(
                "--project",
                project,
                "--output-file",
                tempAssetsToDeleteFile.absolutePath,
                "--filter-file",
                tempFilterFilePath.absolutePath
            )
        )
        val outputRaw = tempAssetsToDeleteFile.readText()
        val output = noComment(outputRaw)
        if (expected != null) {
            assert(expected.all { output.contains(it) }) {
                val missing =expected.filter { !output.contains(it) }
                "missing $expected \nbut output $output\n missing $missing"
            }

            val filteredLines = output.split("\n").filter { it.isNotBlank() }

            assert(expected.size == filteredLines.size) { "expected $expected\noutput $output" }
        }
        return Pair(tempAssetsToDeleteFile, tempFilterFilePath)
    }


    /* After creating, we wait until the assets appear in the unfiltered output.
    After deleting, we wait until they do NOT appear.

     */
    private fun waitForAssets(
        waitCondition: (String, List<String>) -> Boolean, phase: String, expected: List<String>,
    ) {

        val start = currentTimeMillis()
        val timeout = start + timeOutForCreateOrDelete()

        while (true) {
            val allAssets = listAllAssetsForAllSupportedAssetTypes()
            if (!waitCondition(allAssets, expected) || currentTimeMillis() > timeout) {
                break
            }
            Thread.sleep(3000) // To avoid exceeding Asset Service quota
            println("${(currentTimeMillis() - start) / 1000} s waiting for $phase in ${this::class.simpleName}")
        }

        if (currentTimeMillis() >= timeout) {
            throw TimeoutException("Timed out")
        }
    }

    /**
     * Timeout on waiting after an object is created/deleted, since it may take time
     * before this is reflected by the results of the Asset service.
     * Subclasses may give a larger value where times are long, e.g. GKE.
     *
     * But: Note that asset creation in these test blocks, so after asset creation,
     * the asset exists.
     *
     * The only question is whether the Asset Service shows it.
     * Deletion, however, does not block, so the wait is allowing for deletion to complete.
     */
    open fun timeOutForCreateOrDelete(): Long {
        val twoMin = 2 * 60 * 1000L
        return twoMin
    }


    private fun validateAssetTypeIds() {
        FileInputStream(LIST_FILTER_YAML).use { `in` ->
            val rootList = Yaml().loadAll(`in`).toList()
            assert(rootList.size == 1) { rootList.size }
            val filtersFromYaml = rootList.first() as Map<String, Map<String, Any>> //The Any is Boolean|String
            val knownIds = filtersFromYaml.keys.toList()
            val missing = assetTypeIds().filter { !knownIds.contains(it) }
            assert(missing.isEmpty()) { "$missing unknown; see asset-types.properties for valid API identifiers" }

        }
    }


    // todo Use fullpath as identifier for ALL asset types, not just where we have to, as today, where  we
    //  use identifierIsFullPath == true where secondary assets are expected (e.g., GAE Service, GKE Cluster),
    // and then garbage may be left after the test (containers in the case GAE). Clean this garbage up.
    // (The garbage is not special to Cloud Blaster or this test -- it will always happen with such assets.)

    /**
     * We use full path as identifier if secondary assets are expected (e.g., GAE Service which generates containers),
     * because in these cases,the asset name by itself is not enough to identify the asset
     */
    open fun identifierIsFullPath(): Boolean {
        return false
    }


    fun pathForAsset(pattern: String, project: String, name: String, location: String? = null): String {

        val replaced = pattern.replace("{${PROJECT.uppercase(Locale.getDefault())}}", project)
            .replace("{${ID.uppercase(Locale.getDefault())}}", name)
        val ret = if (location == null) {
            replaced
        } else {
            replaced.replace("{${LOCATION.uppercase(Locale.getDefault())}}", location)
        }
        assert(ret != pattern)
        assert(!ret.contains("{"))
        return ret
    }

    fun createAssetByRunningDeployScript(assetName: String, dir: String) {
        assert(assetName.contains(sfx)) { "Pass full asset name with random sfx" }
        val testInput = "test-input"
        if (dir.contains("/$testInput/")) {
            throw IllegalStateException("Do not pass $testInput dir but rather a subdir")
        }
        val workingDir = File("./$testInput/$dir")
        assert((workingDir).isDirectory) { "$workingDir is not a directory" }

        val timeInMillis = measureTimeMillis {
            runCommand("./deploy.sh $project $assetName", workingDir)
        }
        println("Deployed in ${this::class.simpleName} in ${timeInMillis / 1000} s")

    }

    fun makeTempFileFromTemplate(
        assetName: String,
        templateFileBaseName: String,
        subdir: String
    ) {
        assert(!subdir.contains("/$testInput/")) { "pass subdir of $testInput, not including $testInput" }
        val dir = File("./$testInput", subdir)
        val dotTemplate = ".template"
        assert(!templateFileBaseName.endsWith(dotTemplate)) { "Pass base name, not template" }

        val file = File(dir, templateFileBaseName + dotTemplate)
        if (!file.isFile) {
            throw FileNotFoundException(file.absolutePath)
        }
        val templateContent = file.readText()

        val content = templateContent.replace("ASSET_NAME", assetName)


        assert(content != templateContent)
        val outputFile = File(dir, templateFileBaseName)
        outputFile.deleteOnExit()
        FileWriter(outputFile).use { fw ->
            fw.write(content)
        }
    }


}
