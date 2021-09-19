package com.doitintl.blaster.deleter

import java.util.*


//todo Maybe use Kotlin Regex rather than Java Pattern, throughout.
private val UPPERCASE_IN_CURLIES = Regex(""".*(\{[A-Z]+\}).*""")
private val GROUP_NAMES_IN_REGEX = Regex("""\?<([a-zA-Z][a-zA-Z0-9]*)>""")

abstract class AbstractDeleter : AssetDeleter {

    override fun delete(line: String) {
        doDelete(paramsFromPath(line))
    }


    private fun createIdentifierRegex(pathPattern_: String): Regex {
        var pathPattern = pathPattern_.trim()

        if (!pathPattern.startsWith("//")) {
            throw IllegalArgumentException("$pathPattern should start with //")
        }

        while (true) {

            val find = UPPERCASE_IN_CURLIES.find(pathPattern)
            if (find == null) {
                break
            } else {
                val idWIthCurlies = find.groupValues[1]
                assert(idWIthCurlies.toUpperCase() == idWIthCurlies) { idWIthCurlies }
                val id = idWIthCurlies.substring(1, idWIthCurlies.length - 1)
                // The identifiers have constraints beyond just "not-slash" as below.
                // But the goal is to capture the identifer, so a too-broad regex is OK so long as it is accurate and precise.
                val idRegex = "(?<${id.toLowerCase()}>[^/]+)"
                pathPattern = pathPattern.replace(idWIthCurlies, idRegex)
            }
        }

        return Regex(pathPattern)

    }


    override fun paramsFromPath(path: String): Map<String, String> {
        val params: MutableMap<String, String> = HashMap()

        //There's probably a better way to match the multiple regexes than this weird loop
        for (regex in pathRegexes()) {
            val keys = groupNames(regex)
            val matchResult = regex.find(path)
            if (matchResult != null) {
                for (k in keys) {
                    params[k] = matchResult.groups[k]!!.value
                }
                break
            }
        }

        return params
    }

    private fun groupNames(regex: Regex): List<String> {
        assert(GROUP_NAMES_IN_REGEX.matches(regex.pattern))
        val matchResults = GROUP_NAMES_IN_REGEX.findAll(regex.pattern)
        return matchResults.map { it.groups[1]!!.value }.toList().distinct()
    }

    override fun pathRegexes(): List<Regex> {
        return pathPatterns.map(this::createIdentifierRegex)
    }
}