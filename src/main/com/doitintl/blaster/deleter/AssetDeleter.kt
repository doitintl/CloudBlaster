package com.doitintl.blaster.deleter


interface AssetDeleter {
    fun paramsFromPath(path: String): Map<String, String> //subclasses invoke this
    fun pathRegexes(): List<Regex>//invoke this
    val pathPatterns: Array<String>//subclasses implement this
    fun delete(line: String)//invoke this
    fun doDelete(p: Map<String, String>)//subclassess implement this


}