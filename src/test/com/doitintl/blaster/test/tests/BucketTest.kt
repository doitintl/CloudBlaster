package com.doitintl.blaster.test.tests


import com.doitintl.blaster.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.Constants.LIST_FILTER_YAML
import com.doitintl.blaster.lister.LIST_THESE
import com.doitintl.blaster.lister.REGEX
import com.doitintl.blaster.test.randomString
import com.doitintl.blaster.test.runCommand
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.FileWriter
import java.lang.Thread.sleep
import java.util.*
import com.doitintl.blaster.deleter.main as deleter
import com.doitintl.blaster.lister.main as lister

class BucketTest {

    private fun assetTypeIds(): List<String> = listOf("storage.googleapis.com/Bucket")

    private fun createAssets(sfx: String, project: String): List<String> {
        fun makeBucket(project: String, location: String, bucketName: String) {
            runCommand("gsutil mb -p $project -l $location gs://$bucketName")
        }

        val multiregionAsset = "blastertest-multiregion-$sfx"
        val regionAsset = "blastertest-region-$sfx"

        makeBucket(project, "us", multiregionAsset)
        makeBucket(project, "us-central1", regionAsset)
        return listOf(multiregionAsset, regionAsset)
    }


    @Test
    fun createListDeleteTest() {
        val sfx = randomString(8)
        println("Suffix $sfx")
        val project = System.getProperty("PROJECT_ID") ?: "joshua-playground2"

        val assets = creationPhase(sfx, project)
        val (tempAssetToDeleteFile, tempFilterFile) = listResultsWithFilter(sfx, project, assets)
        deletionPhase(tempAssetToDeleteFile, tempFilterFile, project, sfx, assets)
    }

    private fun creationPhase(sfx: String, project: String): List<String> {
        val assets = createAssets(sfx, project)

        val conditionNotAll = { allAssets: String, assets_: List<String> -> !assets_.all { asset -> allAssets.contains(asset) } }

        waitOnUnfilteredOutput(project, sfx, conditionNotAll, assets)
        return assets
    }

    private fun deletionPhase(tempAssetToDeleteFile: String, tempFilterFile: String, project: String, sfx: String, assets: List<String>) {
        deleter(arrayOf("-d", tempAssetToDeleteFile, "-f", tempFilterFile))
        val conditionSome = { allAssets: String, assets_: List<String> -> !assets_.none { asset -> allAssets.contains(asset) } }
        waitOnUnfilteredOutput(project, sfx, conditionSome, assets)
    }


    private fun listResultsWithFilter(sfx: String, project: String, expected: List<String>): Pair<String, String> {
        val tempFilterFilePath = newFilterYaml(sfx)
        val tempAssetsToDeleteFile = createTempFile("$ASSET_LIST_FILE-$sfx", ".txt")
        tempAssetsToDeleteFile.deleteOnExit()
        lister(arrayOf("-p", project, "-o", tempAssetsToDeleteFile.absolutePath, "-f", tempFilterFilePath))
        val output = FileReader(tempAssetsToDeleteFile).readText()

        assert(expected.all { output.contains(it) }) { "expected $expected \nbut output $output" }
        var lines = output.split("\n")
        if (lines[lines.lastIndex].trim() == "") {//remove last, empty line
            lines = lines.subList(0, lines.lastIndex)
        }
        assert(lines.size == expected.size, { "expected $expected\noutput $output" })
        return Pair(tempAssetsToDeleteFile.absolutePath, tempFilterFilePath)
    }


    private fun waitOnUnfilteredOutput(
            project: String,
            sfx: String,
            waitCondition: (String, List<String>) -> Boolean,
            expected: List<String>,
    ) {
        val DECISEC: Long = 100
        val ONE_MIN_IN_DECISEC = 10//plus time for the actual calls
        val outputTempFile = createTempFile("all-assets$sfx", ".txt")
        outputTempFile.deleteOnExit()
        var counter = 0
        do {
            lister(arrayOf("-p", project, "-o", outputTempFile.absolutePath, "-n"))
            val allAssets = File(outputTempFile.absolutePath).readText()

            sleep(DECISEC)
            print(". ")
        } while (counter++ < ONE_MIN_IN_DECISEC && waitCondition(allAssets, expected))
        println()

        val allAssets = File(outputTempFile.absolutePath).readText()

        assert(!waitCondition(allAssets, expected))//assert that we didn't timeout
    }


    private fun newFilterYaml(sfx: String): String {

        var counter = 0
        FileInputStream(LIST_FILTER_YAML).use { `in` ->
            for (o in Yaml().loadAll(`in`)) {

                val filtersFromYaml = o as Map<String, Map<String, Any>>//The inner Map is Boolean|String
                val filtersFromYamlOut = TreeMap<String, Map<String, Any>>()
                for (assetTypeId: String in filtersFromYaml.keys) {
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
                    throw IllegalArgumentException("Only 1 object allowed in root of list-filter")
                }

                return writeTempFilterYaml(sfx, filtersFromYamlOut)
            }
        }
        throw  IllegalStateException("Should not get here")
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

fun main(vararg args: String) {
    BucketTest().createListDeleteTest()
    println("DONE")
}
