package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT

class FirewallDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/global/firewalls/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val computeService = getComputeService()
        val request = computeService.firewalls().delete(p[PROJECT], p[ID])
        val operation = request.execute()
        waitOnGlobalOperation(p[PROJECT]!!, operation)
    }
}