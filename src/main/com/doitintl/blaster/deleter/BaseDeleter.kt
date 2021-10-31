package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.IllegalConfigException
import java.util.*

private val UPPERCASE_IN_CURLIES = Regex(""".*(\{[A-Z]+}).*""")
private val GROUP_NAMES_IN_REGEX = Regex("""\?<([a-zA-Z][a-zA-Z0-9]*)>""")

abstract class BaseDeleter : AssetTypeDeleter {
    companion object {
        const val SLEEP_IN_LOOPS_MS: Long = 500 // In loops while waiting for deletion to end
        const val DONE = "DONE"
    }

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
                if (idWIthCurlies.toUpperCase() != idWIthCurlies) {
                    throw IllegalConfigException("Illegal pattern (expect upper-case identifiers): $pathPattern")
                }
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
                    val matchGroup = matchResult.groups[k]!!
                    params[k] = matchGroup.value
                }
                break
            }
        }

        return params
    }

    private fun groupNames(regex: Regex): List<String> {
        val matchResults = GROUP_NAMES_IN_REGEX.findAll(regex.pattern).toList()
        if (matchResults.isEmpty()) {
            throw IllegalConfigException("$regex. lacks the group names")
        }
        return matchResults.map { val grp = it.groups[1]!!; grp.value }.toList().distinct()
    }

    override fun pathRegexes(): List<Regex> {
        return pathPatterns.map(this::createIdentifierRegex)
    }
}
