package com.doitintl.blaster.deleter

import java.util.regex.Pattern

interface AssetDeleter {

    fun doDelete(p: Map<String, String>)
    fun paramsFromPath(path: String): Map<String, String>
    val pathKeys: Array<String>
    val pathPatterns: Array<String>
    fun pathRegexes(): List<Pattern>
    fun delete(line: String)
}