package com.doitintl.blaster.deleter

import java.util.*
import java.util.regex.Pattern


private val UPPERCASE_IN_CURLIES = Pattern.compile(""".*(\{[A-Z]+\}).*""")

abstract class AbstractDeleter : AssetDeleter {

    override fun delete(line: String) {
        doDelete(paramsFromPath(line))
    }


    private fun createIdentifierRegex(pathPattern_: String): Pattern {
        var regexStr = pathPattern_
        while (true) {
            val m = UPPERCASE_IN_CURLIES.matcher(regexStr)
            regexStr = if (m.matches()) {
                val idWIthCurlies = m.group(1)
                assert(idWIthCurlies.toUpperCase() == idWIthCurlies) { idWIthCurlies }
                val id = idWIthCurlies.substring(1, idWIthCurlies.length - 1)
                // The identifiers have restrictions beyond just "not-slash" as below. But the goal
                // is to capture the identifer, so a too-broad regex is OK so long as it is accurate and precise.
                val idRegex = "(?<${id.toLowerCase()}>[^/]+)"
                regexStr.replace(idWIthCurlies, idRegex)
            } else {
                //no more upper-case values left, can exit
                break
            }
        }
        return Pattern.compile(regexStr)

    }


    override fun paramsFromPath(path: String): Map<String, String> {
        val params: MutableMap<String, String> = HashMap()
        val keys = pathKeys
        //There's probably a better way to match the multiple regexes than this weird loop
        for (pathPattern in pathRegexes()) {
            val matcher = pathPattern.matcher(path)
            if (matcher.matches()) {
                for (k in keys) {
                    params[k] = matcher.group(k) //group() throws exc  on nomatch
                }
                break
            }
        }
        assert(keys.all { params[it] != null }) { "Some expected params not set" }

        return params
    }

    override fun pathRegexes(): List<Pattern> {
        return pathPatterns.map(this::createIdentifierRegex)
    }
}