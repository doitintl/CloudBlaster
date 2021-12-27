package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class HealthCheckDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/regions/{LOCATION}/healthChecks/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val project = p[PROJECT]!!
        val location = p[LOCATION]!!
        val id = p[ID]
        val request = getComputeService().regionHealthChecks().delete(project, location,id)
        val operation = request.execute()
        waitOnRegionalOperation(project, location,operation)

    }


}

