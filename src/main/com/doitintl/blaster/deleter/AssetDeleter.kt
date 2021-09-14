package com.doitintl.blaster.deleter

import java.util.regex.Pattern

interface AssetDeleter {

    fun doDelete(p: Map<String, String>)
    fun setPathPatterns(pathPatterns: List<Pattern>)
    fun paramsFromPath(path: String): Map<String, String>
    val pathKeys: Array<String>


    fun delete(line: String)
}