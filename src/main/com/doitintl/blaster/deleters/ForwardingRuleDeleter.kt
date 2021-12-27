package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.api.services.compute.Compute

class ForwardingRuleDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/regions/{LOCATION}/forwardingRules/{ID}")

    override fun doDelete(p: Map<String, String>) {
        val project = p[PROJECT]!!
        val location = p[LOCATION]!!
        val id = p[ID]!!
        val request  =   getComputeService().forwardingRules().delete(project,location,  id)
        val operation = request.execute()
        waitOnRegionalOperation(project,location, operation)
    }
}


