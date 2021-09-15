package com.doitintl.blaster.shared

import com.doitintl.blaster.deleter.AssetDeleter
import java.util.*
import java.util.regex.Pattern

class AssetType(
    val apiIdentifier: String,
    pathPatterns: List<String>,
    deleterClass: String
) {
    private val pathPatterns: MutableList<Pattern> = ArrayList()
    lateinit var filterRegex: Pattern
        private set

    lateinit var deleterClass: Class<AssetDeleter>
        private set


    //Nullable becase the regex can be omitted in the YAML
    fun setFilterRegex(regex_: String?) {
        var regex = regex_
        if (regex == null || regex.isEmpty()) {
            regex = "$-never-matches-so-we-list-ALL-assets"
        }
        filterRegex = Pattern.compile(regex)
    }

    fun getPathPatterns(): List<Pattern> {
        return pathPatterns
    }

    private fun setPathPatterns(pathPatterns: List<String>) {
         for (pathPattern in pathPatterns) {
            val regex = createIdentifierRegexes(pathPattern)
            this.pathPatterns.add(Pattern.compile(regex))
        }
    }

    private fun createIdentifierRegexes(pathPattern_: String): String {

        var pathPattern = pathPattern_.replace("PROJECT", "(?<project>[a-z0-9-_]+)")
        while (true) {
            val m = UPPER_CASE_WORD.matcher(pathPattern)
            pathPattern = if (m.matches()) {
                val identifier = m.group(1)
                assert(identifier.toUpperCase() == identifier) { identifier }
                pathPattern.replace(identifier, identifierRegex(identifier.toLowerCase()))
            } else {
                break
            }
        }
        return pathPattern
    }

    private fun identifierRegex(identifier: String ): String {
        // For some asset types, some of these chars are illegal. But the goal
        // is to capture the identifer, so a too-broad regex is OK so long as it is accurate and precise.
        return "(?<$identifier>[a-z0-9-_.~%+]+)"
    }

    private fun setDeleterClass(deleterClass: String?) {
        if (deleterClass == null) {
            return
        }
        val cls: String = if (deleterClass.contains(".")) deleterClass else "com.doitintl.blaster.deleter.$deleterClass"
        this.deleterClass = Class.forName(cls) as Class<AssetDeleter>

    }

    override fun toString(): String {
        return "$apiIdentifier,$filterRegex,${getPathPatterns()},$deleterClass"
    }

    companion object {
        private val UPPER_CASE_WORD = Pattern.compile(".*\\b([A-Z]+)\\b.*")
    }

    init {

        setPathPatterns(pathPatterns)
        setDeleterClass(deleterClass)
    }
}