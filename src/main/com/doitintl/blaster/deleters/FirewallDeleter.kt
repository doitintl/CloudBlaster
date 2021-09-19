package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.PROJECT

class FirewallDeleter : GCEAbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/global/firewalls/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
        val request = computeService.firewalls().delete(p[PROJECT], p[ID])
        val response = request.execute()
    }
}