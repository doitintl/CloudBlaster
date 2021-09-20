package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GAEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.doitintl.blaster.shared.Constants.SERVICE

class GAEVersionDeleter : GAEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//appengine.googleapis.com/apps/{PROJECT}/services/{SERVICE}/versions/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val engine = getAppEngine()
        val operation = engine.apps().services().versions().delete(p[PROJECT], p[SERVICE], p[ID]).execute()
        waitOnOperation(p[PROJECT]!!, operation)

    }


}