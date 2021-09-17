package com.doitintl.blaster.lister

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
                        throw RuntimeException("Only one pattern should match each path: ${ret.javaClass.name} and ${assetType.deleterClass.name} both found for pattern $regex")
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

    private operator fun get(assetTypeIdentifier: String): AssetType {
        return assetTypeMap[assetTypeIdentifier] ?: error(assetTypeIdentifier + "not found")
    }

    fun getFilterRegex(assetTypeIdentifier: String): Pattern {
        return get(assetTypeIdentifier).filterRegex

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
                for ((k, v) in props.entries) {
                    val at = assetTypeMap_[k]!!
                    at.setFilterRegex(v as String)
                }
            }
        }


        private fun loadAssetTypesFile(): Map<String, AssetType> {
            val ret: MutableMap<String, AssetType> = TreeMap()
            FileInputStream(ASSET_TYPES_FILE).use { `in` ->
                val props = Properties()
                props.load(`in`)
                for ((k, v) in props.entries) {
                    val assetTypeId = k as String
                    if (!assetTypePattern.matcher(assetTypeId).matches()) {
                        throw  IllegalArgumentException("Unsupported asset type id $assetTypeId")
                    }

                    val optionalDeleterClassName = v as String
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


private class AssetType(
    val assetTypeId: String,
    deleterClassName: String
) {
    lateinit var filterRegex: Pattern
        private set

    lateinit var deleterClass: Class<AssetDeleter>
        private set


    //NonNullable param even thought the regex can be omitted in that config file (and you get a blank string)
    fun setFilterRegex(regex: String) {
        filterRegex = Pattern.compile(
            if (regex.isBlank()) {
                regex
            } else {
                "$-never-matches-so-we-list-ALL-assets"
            }
        )
    }

    private fun setDeleterClass(deleterClassName_: String) {
        fun deleterClassName(assetTypeId: String, optionalClassName: String): String {
            val className =
                if (optionalClassName.isBlank()) {//use defaults
                    val parts = assetTypeId.split("/").toTypedArray()
                    val assetTypeShortName = parts[parts.size - 1]
                    assetTypeShortName + "Deleter"
                } else {
                    optionalClassName
                }
            return if (className.contains(".")) className else "com.doitintl.blaster.deleters.$className"
        }

        val deleterClassName = deleterClassName(assetTypeId, deleterClassName_)
        this.deleterClass = Class.forName(deleterClassName) as Class<AssetDeleter>
    }


    override fun toString(): String {
        return "$assetTypeId,$filterRegex,$deleterClass"
    }

    init {
        setDeleterClass(deleterClassName)
    }
}