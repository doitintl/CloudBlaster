package com.doitintl.blaster.deleter

import java.util.*
import java.util.regex.Pattern

abstract class AbstractDeleter : AssetDeleter {
    private var pathPatterns: List<Pattern?>? = null
    override fun setPathPatterns(pathPatterns: List<Pattern?>?) {
        this.pathPatterns = pathPatterns
    }


    override fun delete(line: String?) {
        doDelete(paramsFromPath(line))
    }

    override fun paramsFromPath(path: String?): Map<String?, String?> {
        val params: MutableMap<String?, String?> = HashMap()
        val keys = pathKeys
        //There's probably a better way to match the multiple regexes than this weird loop
        for (pathPattern in pathPatterns!!) {
            val matcher = pathPattern!!.matcher(path!!)
            if (matcher.matches()) {
                for (k in keys) {
                    params[k] = matcher.group(k) //group() throws exc  on nomatch
                }
                break
            }
        }
        assert(keys.all { it -> params[it] != null })

        return params
    }
}