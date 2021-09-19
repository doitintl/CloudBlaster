package com.doitintl.blaster.lister

import com.doitintl.blaster.deleter.AssetDeleter
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileReader
import java.util.*
import java.util.stream.Collectors

val assetTypeRegex: Regex = Regex("""[a-z]+\.googleapis\.com/[a-zA-Z]+""")
const val REGEX = "regex"
const val LIST_THESE = "listThese"

class AssetTypeMap(private val filterFile: String) {

    private val assetTypeMap: Map<String, AssetType>

    fun deleterClass(line: String): AssetDeleter {
        var ret: AssetDeleter? = null
        for (assetType in assetTypeMap.values) {
            val deleter = assetType.deleterClass.getConstructor().newInstance()
            for (regex in deleter.pathRegexes()) {

                if (regex.matches(line.trim())) {
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

    fun getFilterRegex(assetTypeIdentifier: String): Pair<Regex, Boolean> {
        val at = get(assetTypeIdentifier)
        return Pair(at.filterRegex, at.regexIsWhitelist)

    }


    private fun loadAssetTypesFromFile(): Map<String, AssetType> {
        compareKeys()
        val ret = loadAssetTypesFile()
        loadListFilterFile(ret) //inout
        return ret
    }

    private fun loadListFilterFile(assetTypeMap_inout: Map<String, AssetType>) {

        FileInputStream(filterFile).use { `in` ->
            var count = 0
            for (o in Yaml().loadAll(`in`)) {

                count++
                if (count > 1) {
                    throw java.lang.IllegalArgumentException("Should have only one root-level map in the $filterFile")
                }
                val filtersFromYaml = o as Map<String, Map<String, Any>>//The inner Map is Boolean|String
                for (assetTypeId in filtersFromYaml.keys) {
                    val filter = filtersFromYaml[assetTypeId]!!
                    val sz = filter.size
                    val error =
                        "$assetTypeId has keys ${filter.keys.toList()} but should have either none or \"$REGEX\" and \"$LIST_THESE\""
                    val at = assetTypeMap_inout[assetTypeId] ?: error("$assetTypeId not found")
                    when (sz) {
                        0 -> {
                            at.setFilterRegex(".*", true)
                        }
                        2 -> {
                            if (filter.keys.toSet() != setOf(REGEX, LIST_THESE)) {
                                throw IllegalArgumentException(error)
                            } else {
                                val listThese = filter[LIST_THESE]!! as Boolean
                                val regexS = (filter[REGEX]!! as String).trim()

                                if (regexS.isEmpty()) {
                                    throw IllegalArgumentException("$assetTypeId has blank $REGEX. Either omit $REGEX and $LIST_THESE or give a value")
                                }
                                at.setFilterRegex(regexS, listThese)
                            }
                        }
                        else -> throw IllegalArgumentException(error)

                    }
                }
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
                if (!assetTypeRegex.matches(assetTypeId)) {
                    throw  IllegalArgumentException("Unsupported asset type id $assetTypeId")
                }

                val optionalDeleterClassName = v as String
                ret[assetTypeId] = AssetType(assetTypeId, optionalDeleterClassName)
            }
        }
        return ret
    }


    private fun compareKeys() {
        fun loadAssetTypeIdsFromProps(fileName: String): List<String> {
            FileReader(fileName).use { `in` ->
                val props = Properties()
                props.load(`in`)
                return props.keys.map { it as String }
            }
        }

        fun loadAssetTypeIdsFromYaml(fileName: String): List<String> {
            FileReader(fileName).use { `in` ->
                for (o in Yaml().loadAll(`in`)) {
                    //only one object in root
                    val filtersFromYaml = o as Map<String, Map<String, Any>>
                    return filtersFromYaml.keys.toList()
                }
            }
            throw IllegalStateException("Should not reach here")
        }


        var errMessage = ""
        val at = loadAssetTypeIdsFromProps(ASSET_TYPES_FILE)
        val lf = loadAssetTypeIdsFromYaml(filterFile)

        val missingInFilter = at subtract lf
        if (missingInFilter.isNotEmpty()) {
            errMessage += "Missing in $filterFile: $missingInFilter\n"
        }
        val missingInAssetTypes = lf subtract at
        if (missingInAssetTypes.isNotEmpty()) {
            errMessage += "Missing in ${ASSET_TYPES_FILE}: $missingInAssetTypes"
        }
        check(errMessage.isEmpty()) { errMessage }

    }


    init {
        assetTypeMap = loadAssetTypesFromFile()
    }

    companion object {
        private const val ASSET_TYPES_FILE = "asset-types.properties"
    }
}


private class AssetType(
    val assetTypeId: String,
    deleterClassName: String
) {
    lateinit var filterRegex: Regex
        private set

    var regexIsWhitelist: Boolean = false
        private set

    lateinit var deleterClass: Class<AssetDeleter>
        private set


    //NonNullable param even thought the regex can be omitted in that config file (and you get a blank string)
    fun setFilterRegex(regex: String, listThese: Boolean) {

        filterRegex = Regex(regex)
        regexIsWhitelist = listThese
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