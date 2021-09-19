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

import java.io.FileInputStream
import java.io.FileReader
import java.io.FileWriter
import java.lang.Thread.sleep
import java.util.*
import kotlin.test.assertEquals
import com.doitintl.blaster.lister.main as lister


class BucketTest {

    private fun makeBucket(project: String, location: String, bucketName: String) {
        val command = "gsutil mb -p $project -l $location gs://$bucketName"
        runCommand(command)
    }

    @Test
    fun bucketTest() {
        val sfx = randomString(8)
        println("Suffix $sfx")
        val project = System.getProperty("PROJECT_ID") ?: "joshua-playground2"
        val multiregionAsset = "blastertest-multiregion-$sfx"
        val regionAsset = "blastertest-region-$sfx"

        makeBucket(project, "us", multiregionAsset)
        makeBucket(project, "us-central1", regionAsset)

        waitForCreationBasedOnUnfilteredOutput(project, sfx, regionAsset, multiregionAsset)

        listResultsWithFilter(sfx, project, listOf(regionAsset, multiregionAsset))
    }

    private fun listResultsWithFilter(sfx: String, project: String, expected: List<String>) {
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
        assertEquals(lines.size, expected.size, "expected $expected\noutput $output")

    }


    private fun waitForCreationBasedOnUnfilteredOutput(
            project: String,
            sfx: String,
            vararg expected: String
    ) {
        val outputTempFile = createTempFile("all-assets$sfx", ".txt")
        outputTempFile.deleteOnExit()
        var counter = 0
        do {
            lister(arrayOf("-p", project, "-o", outputTempFile.absolutePath, "-n"))
            val allAssets = FileReader(outputTempFile).readText()//should close FR

            sleep(100)
        } while (counter++ < 100 && !expected.all { allAssets.contains(it) })
        val allAssets = FileReader(outputTempFile).readText()

        assert(expected.all { allAssets.contains(it) }) { "counter $counter, expected $expected,\nall $allAssets" }

    }

    private fun newFilterYaml(sfx: String): String {

        var counter = 0
        FileInputStream(LIST_FILTER_YAML).use { `in` ->
            for (o in Yaml().loadAll(`in`)) {

                val filtersFromYaml = o as Map<String, Map<String, Any>>//The inner Map is Boolean|String
                val filtersFromYamlOut = TreeMap<String, Map<String, Any>>()
                for (assetTypeId in filtersFromYaml.keys) {
                    val value: Map<String, Any> =
                            if (assetTypeId == "storage.googleapis.com/Bucket") {
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
                return writeYaml(filtersFromYamlOut, sfx)
            }
        }
        throw  IllegalStateException("Should not get here")
    }

    private fun writeYaml(newYaml: Map<String, Map<String, Any>>, sfx: String): String {
        val baseFilename = LIST_FILTER_YAML.split(".")[0]
        val fileName = "$baseFilename-$sfx"
        val tempFile = createTempFile(fileName, ".yaml")
        tempFile.deleteOnExit()

        val dumperOptions = DumperOptions()
        dumperOptions.isPrettyFlow = true
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        dumperOptions.defaultScalarStyle = DumperOptions.ScalarStyle.DOUBLE_QUOTED
        val yaml = Yaml(dumperOptions)

        yaml.dump(newYaml, FileWriter(tempFile))//should close FW

        return tempFile.absolutePath
    }

}

