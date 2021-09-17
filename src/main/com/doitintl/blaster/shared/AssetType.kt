package com.doitintl.blaster.shared

import com.doitintl.blaster.deleter.AssetDeleter
import java.util.regex.Pattern

class AssetType(
    val assetTypeId: String,
    deleterClassName: String
) {
    lateinit var filterRegex: Pattern
        private set

    lateinit var deleterClass: Class<AssetDeleter>
        private set


    //Nullable becase the regex can be omitted in that config file
    fun setFilterRegex(regex_: String?) {
        var regex = regex_
        if (regex == null || regex.isEmpty()) {
            regex = "$-never-matches-so-we-list-ALL-assets"
        }
        filterRegex = Pattern.compile(regex)
    }

    private fun setDeleterClass(deleterClassName_: String) {
        val deleterClassName = deleterClassName(assetTypeId, deleterClassName_)
        this.deleterClass = Class.forName(deleterClassName) as Class<AssetDeleter>
    }


    private fun deleterClassName(assetTypeId: String, optionalClassName: String): String {
        val className =
            if (optionalClassName.isBlank()) {//use defaults
                val parts = assetTypeId.split("/").toTypedArray()
                val assetTypeShortName = parts[parts.size - 1]
                assetTypeShortName + "Deleter"
            } else {
                optionalClassName
            }
        return if (className.contains(".")) className else "com.doitintl.blaster.deleter.$className"
    }

    override fun toString(): String {
        return "$assetTypeId,$filterRegex,$deleterClass"
    }

    init {
        setDeleterClass(deleterClassName)
    }
}