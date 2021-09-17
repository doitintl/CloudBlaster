package com.doitintl.blaster.shared

import com.doitintl.blaster.deleter.AssetDeleter
import java.io.FileInputStream
import java.io.FileReader
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

val assetTypePattern: Pattern = Pattern.compile("""[a-z]+\.googleapis\.com/[a-zA-Z]+""")

class AssetTypeMap private constructor() {

    private val assetTypeMap: Map<String, AssetType>

    fun deleterClass(line: String): AssetDeleter {
        var ret: AssetDeleter? = null
        for (assetType in assetTypeMap.values) {
            val deleter = assetType.deleterClass.getConstructor().newInstance()
            for (regex in deleter.pathRegexes()) {
                val matcher = regex.matcher(line.trim())
                if (matcher.matches()) {
                    if (ret != null) {
                        throw RuntimeException("We do not expect multiple patterns to match one path: $ret and $assetType both found for pattern $regex")
                    }
                    ret = deleter
                }
            }
        }

        assert(ret != null) { "Did not match any path-pattern $line" }

        return ret!!
    }

    fun identifiers(): List<String> {
        return assetTypeMap.values.stream().map { it.assetTypeId }.collect(Collectors.toList())
    }

    operator fun get(assetTypeIdentifier: String): AssetType {
        return assetTypeMap[assetTypeIdentifier] ?: error(assetTypeIdentifier + "not found")
    }

    companion object {
        val instance = AssetTypeMap()
        private const val ASSET_TYPES_FILE = "asset-types.properties"
        private const val LIST_FILTER_PROPERTIES = "list-filter.properties"
        private fun loadAssetTypesFromFile(): Map<String, AssetType> {
            compareKeys()
            val ret = loadAssetTypesFile()
            loadListFilterFile(ret) //inout
            return ret
        }

        private fun loadListFilterFile(assetTypeMap_: Map<String, AssetType>) {
            FileReader(LIST_FILTER_PROPERTIES).use { `in` ->
                val props = Properties()
                props.load(`in`)
                for (e in props.entries) {
                    val at = assetTypeMap_[e.key]!!
                    at.setFilterRegex(e.value as String)
                }
            }
        }


        private fun loadAssetTypesFile(): Map<String, AssetType> {
            val ret: MutableMap<String, AssetType> = TreeMap()
            FileInputStream(ASSET_TYPES_FILE).use { `in` ->
                val props = Properties()
                props.load(`in`)
                for (e in props.entries) {
                    val assetTypeId = e.key as String
                    if (!assetTypePattern.matcher(assetTypeId).matches()) {
                        throw  IllegalArgumentException("Unsupported asset type id $assetTypeId")
                    }

                    val optionalDeleterClassName = e.value as String
                    ret[assetTypeId] = AssetType(assetTypeId, optionalDeleterClassName)
                }
            }
            return ret
        }


        private fun compareKeys() {
            fun loadAssetTypeIds(fileName: String): List<String> {
                FileReader(fileName).use { `in` ->
                    val props = Properties()
                    props.load(`in`)
                    return props.keys.map { it as String }
                }
            }

            var errMessage = ""
            val at = loadAssetTypeIds(ASSET_TYPES_FILE)
            val lf = loadAssetTypeIds(LIST_FILTER_PROPERTIES)

            val missingInFilter = at subtract lf
            if (missingInFilter.isNotEmpty()) {
                errMessage += "Missing in $LIST_FILTER_PROPERTIES: $missingInFilter\n"
            }
            val missingInAssetTypes = lf subtract at
            if (missingInAssetTypes.isNotEmpty()) {
                errMessage += "Missing in $ASSET_TYPES_FILE: $missingInAssetTypes"
            }
            check(errMessage.isEmpty()) { errMessage }

        }

    }

    init {
        assetTypeMap = loadAssetTypesFromFile()
    }
}