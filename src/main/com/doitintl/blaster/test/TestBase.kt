package com.doitintl.blaster.test

import com.doitintl.blaster.lister.LIST_THESE
import com.doitintl.blaster.lister.REGEX
import com.doitintl.blaster.shared.Constants
import com.doitintl.blaster.shared.noComment
import com.doitintl.blaster.shared.randomString
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileWriter
import java.util.*
import com.doitintl.blaster.deleter.main as deleter
import com.doitintl.blaster.lister.main as lister

abstract class TestBase(val project: String, private val sfx: String = randomString(8)) {
    abstract fun createAssets(sfx: String, project: String): List<String>
    abstract fun assetTypeIds(): List<String>

    init {
        try {
            assert(false)
            println("Must enable assertions")
            System.exit(1)
        } catch (ae: AssertionError) {
            //ok
        }
    }

    fun createListDeleteTest(project: String) {


        val assets = creationPhase(sfx, project)
        val (tempAssetToDeleteFile, tempFilterFile) = listResultsWithFilter(sfx, project, assets)
        deletionPhase(tempAssetToDeleteFile, tempFilterFile, assets)
    }

    fun assetName(type: String): String {
        return "blastertest-$type-$sfx"
    }

    private fun creationPhase(sfx: String, project: String): List<String> {
        val assets = createAssets(sfx, project)

        val conditionNotAll = { allAssets: String, assets_: List<String> -> !assets_.all { asset -> allAssets.contains(asset) } }

        waitOnUnfilteredOutput(conditionNotAll, assets)
        return assets
    }


    private fun deletionPhase(tempAssetToDeleteFile: String, tempFilterFile: String, assets: List<String>) {
        deleter(arrayOf("-d", tempAssetToDeleteFile, "-f", tempFilterFile))
        val conditionSome = { allAssets: String, assets_: List<String> -> !assets_.none { asset -> allAssets.contains(asset) } }
        waitOnUnfilteredOutput(conditionSome, assets)
    }


    private fun listResultsWithFilter(sfx: String, project: String, expected: List<String>): Pair<String, String> {
        val tempFilterFilePath = newFilterYaml(sfx)
        val tempAssetsToDeleteFile = createTempFile("${Constants.ASSET_LIST_FILE}-$sfx", ".txt")
        tempAssetsToDeleteFile.deleteOnExit()
        lister(arrayOf("-p", project, "-o", tempAssetsToDeleteFile.absolutePath, "-f", tempFilterFilePath))
        val outputRaw = tempAssetsToDeleteFile.readText()
        val output = noComment(outputRaw)
        assert(expected.all { output.contains(it) }) { "expected $expected \nbut output $output" }
        var lines = output.split("\n").filter { it.isNotBlank() }

        //can have an extra line when a disk is generated alongside its instnace
        assert(lines.size == expected.size || lines.size == expected.size + 1) { "expected $expected\noutput $output" }
        return Pair(tempAssetsToDeleteFile.absolutePath, tempFilterFilePath)
    }


    private fun waitOnUnfilteredOutput(

            waitCondition: (String, List<String>) -> Boolean,
            expected: List<String>,
    ) {
        val DECISEC: Long = 100
        val DECISEC_IN_MIN = 10
        val minutes = 3//plus time for the code to run, so more time than that
        val loopLimit = minutes * DECISEC_IN_MIN
        val outputTempFile = createTempFile("all-assets$sfx", ".txt")
        outputTempFile.deleteOnExit()
        var counter = 0
        do {
            lister(arrayOf("-p", project, "-o", outputTempFile.absolutePath, "-n"))
            val allAssets = outputTempFile.readText()

            Thread.sleep(DECISEC)
            print(". ")
        } while (counter++ < loopLimit && waitCondition(allAssets, expected))
        println()
        assert(counter <= loopLimit, { "Timed out" })
    }


    private fun newFilterYaml(sfx: String): String {
        var counter = 0
        FileInputStream(Constants.LIST_FILTER_YAML).use { `in` ->
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
        val baseFilename = Constants.LIST_FILTER_YAML.split(".")[0]
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