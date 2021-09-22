package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GAEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT

class GAEServiceDeleter : GAEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//appengine.googleapis.com/apps/{PROJECT}/services/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val project = p[PROJECT]!!
        val id = p[ID]
        val operation = getAppEngine().apps().services().delete(project, id).execute()

        waitOnOperation(project, operation)

    }


}