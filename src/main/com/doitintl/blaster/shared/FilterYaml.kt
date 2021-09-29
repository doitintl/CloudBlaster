package com.doitintl.blaster.shared

import com.doitintl.blaster.lister.LIST_THESE
import com.doitintl.blaster.lister.REGEX
import com.doitintl.blaster.shared.Constants.LIST_FILTER_YAML
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.*

fun writeTempFilterYaml(assetTypeIds: List<String>, sfx: String): File {
    FileInputStream(LIST_FILTER_YAML).use { `in` ->
        for (o in Yaml().loadAll(`in`)) {
            val filtersFromYaml = o as Map<String, Map<String, Any>>//The inner Map is Boolean|String
            val filtersFromYamlOut = TreeMap<String, Map<String, Any>>()
            for (assetTypeId: String in filtersFromYaml.keys) {
                val filterForThisAssetType = if (assetTypeIds.contains(assetTypeId)) {
                    val findAll = if (sfx.isBlank()) {
                        ".*"
                    } else {
                        ".*$sfx.*"
                    }
                    mapOf(REGEX to findAll, LIST_THESE to true)
                } else {
                    mapOf(REGEX to ".*", LIST_THESE to false)
                }
                filtersFromYamlOut[assetTypeId] = filterForThisAssetType

            }

            return writeTempFilterYaml0(filtersFromYamlOut, sfx)
        }
    }
    throw IllegalCodePathException("Should not get here")
}

private fun writeTempFilterYaml0(
    filtersFromYaml: TreeMap<String, Map<String, Any>>,
    sfx: String? = null
): File {
    val baseFilename = LIST_FILTER_YAML.split(".")[0]
    val suffix = if (sfx != null) {
        "-$sfx"
    } else {
        ""
    }
    val tempFilterYaml = createTempFile("$baseFilename$suffix", ".yaml")
    tempFilterYaml.deleteOnExit()
    writeYamlToFile(tempFilterYaml, filtersFromYaml)
    return tempFilterYaml
}

private fun writeYamlToFile(file: File, newYaml: Map<String, Map<String, Any>>) {
    val dumperOptions = DumperOptions()
    dumperOptions.isPrettyFlow = true
    dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    dumperOptions.defaultScalarStyle = DumperOptions.ScalarStyle.DOUBLE_QUOTED
    val yaml = Yaml(dumperOptions)
    FileWriter(file).use { fw ->
        yaml.dump(newYaml, fw)
    }
}
