package com.doitintl.blaster.shared

import com.doitintl.blaster.deleter.AssetDeleter
import java.util.*
import java.util.regex.Pattern

class AssetType(
    apiIdentifier: String?,
    pathPatterns: List<String>?,
    deleterClass: String?,
    otherLegalCharsInId: String?
) {
    private val pathPatterns: MutableList<Pattern> = ArrayList()
    val apiIdentifier: String?
    var filterRegex: Pattern? = null
        private set
    var deleterClass: Class<AssetDeleter>? = null
        private set

    fun supportedForDeletion(): Boolean {
        return deleterClass != null && getPathPatterns().isNotEmpty()
    }

    fun setFilterRegex(regex_: String?) {
        var regex = regex_
        if (regex == null || "" == regex) {
            regex = "$-never-matches-so-we-list-ALL-resources"
        }
        filterRegex = Pattern.compile(regex)
    }

    fun getPathPatterns(): List<Pattern> {
        return pathPatterns
    }

    private fun setPathPatterns(pathPatterns: List<String>?, otherLegalCharsInId_: String?) {
        val otherLegalCharsInId = otherLegalCharsInId_ ?: ""
        for (pathPattern in pathPatterns!!) {
            val regex = createIdentifierRegexes(pathPattern, otherLegalCharsInId)
            val p = Pattern.compile(regex)
            this.pathPatterns.add(p)
        }
    }

    private fun createIdentifierRegexes(pathPattern_: String, otherLegalCharsInId: String): String {

        var pathPattern = pathPattern_.replace("PROJECT", "(?<project>[a-z0-9-_]+)")
        while (true) {
            val m = UPPER_CASE_WORD.matcher(pathPattern)
            pathPattern = if (m.matches()) {
                val identifier = m.group(1)
                assert(identifier.toUpperCase() == identifier) { identifier }
                pathPattern.replace(identifier, identifierRegex(identifier.toLowerCase(), otherLegalCharsInId))
            } else {
                break
            }
        }
        return pathPattern
    }

    private fun identifierRegex(identifier: String, otherLegalCharsInId: String?): String {
        assert(otherLegalCharsInId != null)
        return "(?<$identifier>[a-z0-9-_$otherLegalCharsInId]+)"
    }

    private fun setDeleterClass(deleterClass: String?) {
        if (deleterClass == null) {
            return
        }
        val cls: String = if (deleterClass.contains(".")) deleterClass else "com.doitintl.blaster.deleter.$deleterClass"
        try {
            this.deleterClass = Class.forName(cls) as Class<AssetDeleter>
        } catch (cnfe: ClassNotFoundException) {
            throw RuntimeException(cnfe)
        }
    }

    override fun toString(): String {
        return "$apiIdentifier,$filterRegex,${getPathPatterns()},$deleterClass"
    }

    companion object {
        private val UPPER_CASE_WORD = Pattern.compile(".*\\b([A-Z]+)\\b.*")
    }

    init {
        assert(apiIdentifier != null)
        assert(pathPatterns != null)
        this.apiIdentifier = apiIdentifier
        setPathPatterns(pathPatterns, otherLegalCharsInId)
        setDeleterClass(deleterClass)
    }
}