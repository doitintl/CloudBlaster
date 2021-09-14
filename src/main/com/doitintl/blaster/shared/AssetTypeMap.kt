package com.doitintl.blaster.shared

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.util.*
import java.util.stream.Collectors

class AssetTypeMap private constructor() {
    private val assetTypeMap: Map<String, AssetType>
    fun pathToAssetType(line: String): AssetType? {
        var ret: AssetType? = null
        for (assetType in assetTypeMap.values) {
            for (regex in assetType.getPathPatterns()) {
                val matcher = regex.matcher(line.trim())
                if (matcher.matches()) {
                    if (ret != null) {
                        throw RuntimeException("We do not expect multiple patterns to match one path: $ret and $assetType both found for pattern $regex")
                    }
                    ret = assetType
                }
            }
        }
        assert(ret != null) { "Did not match any path-pattern $line" }
        return ret
    }

    fun identifiers(): List<String> {
        return assetTypeMap.values.stream().map { obj: AssetType -> obj.apiIdentifier }.collect(Collectors.toList())
    }

    operator fun get(assetTypeIdentifier: String): AssetType {
        return assetTypeMap[assetTypeIdentifier] ?: error(assetTypeIdentifier + "not found")
    }

    companion object {
        val instance = AssetTypeMap()
        private const val ASSET_TYPES_YAML = "asset-types.yaml"
        private const val LIST_FILTER_YAML = "list-filter.yaml"
        private fun loadAssetTypesFromYaml(): Map<String, AssetType> {

            compareYamls()
            val map = loadAssetTypesYaml()
            loadListFilterYaml(map) //inout
            return map

        }


        private fun loadListFilterYaml(map: Map<String, AssetType>) {
            FileInputStream(LIST_FILTER_YAML).use { `in` ->
                for (o in Yaml().loadAll(`in`)) {
                    for (e in (o as Map<String, String>).entries) {
                        val at = map[e.key]
                            ?: error("All asset types in $LIST_FILTER_YAML should have been in $ASSET_TYPES_YAML")
                        at.setFilterRegex(e.value)
                    }
                }
            }
        }


        private fun loadAssetTypesYaml(): Map<String, AssetType> {
            val ret: MutableMap<String, AssetType> = TreeMap()
            FileInputStream(ASSET_TYPES_YAML).use { `in` ->
                for (o in Yaml().loadAll(`in`)) {
                    val assetTypesFromYaml = o as Map<String, Map<String, Any>>
                    for (assetTypeId in assetTypesFromYaml.keys) {
                        val assetType = assetTypesFromYaml[assetTypeId] ?: error("$assetTypeId not found")
                        val deleterClass = deleterClassName(assetTypeId, assetType)
                        val pathPatterns = pathPatterns(assetType)
                        val otherLegalCharsInId = assetType["otherLegalCharsInId"] as String?
                        ret[assetTypeId] = AssetType(assetTypeId, pathPatterns, deleterClass, otherLegalCharsInId)
                    }
                }
                return ret
            }
        }

        private fun deleterClassName(k: String, aType: Map<String, Any>): String {
            var deleterClass = aType["deleterClass"] as String?
            if (deleterClass == null) {//use defaults because unspecified
                val parts = k.split("/").toTypedArray()
                val assetTypeShortName = parts[parts.size - 1]
                deleterClass = assetTypeShortName + "Deleter"
            }
            return deleterClass
        }

        private fun compareYamls() {

            var errMessage = ""
            val at = loadAssetTypeIds(ASSET_TYPES_YAML)
            val lf = loadAssetTypeIds(LIST_FILTER_YAML)

            val missingInFilter = at subtract lf
            if (missingInFilter.isNotEmpty()) {
                errMessage += "Missing in $LIST_FILTER_YAML: $missingInFilter"
            }
            val missingInAssetTypes = lf subtract at
            if (missingInAssetTypes.isNotEmpty()) {
                errMessage += "Missing in $ASSET_TYPES_YAML: $missingInAssetTypes"
            }
            check(errMessage.isEmpty()) { errMessage }

        }


        private fun loadAssetTypeIds(fileName: String): Set<String> {
            FileInputStream(fileName).use { `in` ->
                val yaml = Yaml()
                val itr = yaml.loadAll(`in`)
                for (o in itr) { //only 1 root obj in this YAML
                    val assetTypesFromYaml = o as Map<String, Map<*, *>>
                    return assetTypesFromYaml.keys
                }
            }
            throw IllegalArgumentException("Bad YAML")
        }

        private fun pathPatterns(aType: Map<String, Any>): List<String> {
            val pathPatterns: MutableList<String> = ArrayList()

            when (val pathPatternObj = aType["pathPattern"]) { //can be null
                null -> {
                    //pathPatterns is be empty and later asserted as broken
                }
                is String -> {
                    pathPatterns.add(pathPatternObj)
                }
                is List<*> -> {
                    for (pathPatternStr in pathPatternObj) {
                        pathPatterns.add(pathPatternStr as String)
                    }
                }
                else -> {
                    throw IllegalStateException("Unexpected type: $pathPatternObj")
                }
            }
            return pathPatterns
        }
    }

    init {
        assetTypeMap = loadAssetTypesFromYaml()
    }
}