package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class GCEInstanceDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/zones/{LOCATION}/instances/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val operation = getComputeService().instances().delete(p[PROJECT]!!, p[LOCATION]!!, p[ID]!!).execute()
        waitOnZoneOperation(p[PROJECT]!!, p[LOCATION]!!, operation)
    }


}