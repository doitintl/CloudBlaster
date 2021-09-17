package com.doitintl.blaster.deleter

import java.util.*
import java.util.regex.Pattern

//todo Maybe use Kotlin Regex rather than Java Pattern, throughout.
private val UPPERCASE_IN_CURLIES = Pattern.compile(""".*(\{[A-Z]+\}).*""")
private val GROUP_NAMES_IN_REGEX = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>")
abstract class AbstractDeleter : AssetDeleter {

    override fun delete(line: String) {
        doDelete(paramsFromPath(line))
    }


    private fun createIdentifierRegex(pathPattern_: String): Pattern {
        var pathPattern = pathPattern_.trim()

        if (!pathPattern.startsWith("//")) {
            throw IllegalArgumentException("$pathPattern should start with //")
        }

        while (true) {
            val m = UPPERCASE_IN_CURLIES.matcher(pathPattern)
            pathPattern = if (m.matches()) {
                val idWIthCurlies = m.group(1)
                assert(idWIthCurlies.toUpperCase() == idWIthCurlies) { idWIthCurlies }
                val id = idWIthCurlies.substring(1, idWIthCurlies.length - 1)
                // The identifiers have restrictions beyond just "not-slash" as below. But the goal
                // is to capture the identifer, so a too-broad regex is OK so long as it is accurate and precise.
                val idRegex = "(?<${id.toLowerCase()}>[^/]+)"
                pathPattern.replace(idWIthCurlies, idRegex)
            } else {
                //no more upper-case values left, can exit
                break
            }
        }
        return Pattern.compile(pathPattern)

    }


    override fun paramsFromPath(path: String): Map<String, String> {
        val params: MutableMap<String, String> = HashMap()

        //There's probably a better way to match the multiple regexes than this weird loop
        for (pathPattern in pathRegexes()) {
            val keys = groupNames(pathPattern)
            val matcher = pathPattern.matcher(path)
            if (matcher.matches()) {
                for (k in keys) {
                    params[k] = matcher.group(k) //group() throws exc  on nomatch
                }
                break
            }
        }

        return params
    }

    private fun groupNames(pathPattern: Pattern): Set<String> {
        val namedGroups: MutableSet<String> = TreeSet<String>()

        val matcher = GROUP_NAMES_IN_REGEX.matcher(pathPattern.pattern())
        while (matcher.find()) {
            namedGroups.add(matcher.group(1))
        }

        return namedGroups

    }

    override fun pathRegexes(): List<Pattern> {
        return pathPatterns.map(this::createIdentifierRegex)
    }
}