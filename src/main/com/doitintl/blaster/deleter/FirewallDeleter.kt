package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class FirewallDeleter : GCEAbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/global/firewalls/{ID}")



    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
         val request = computeService.firewalls().delete(p[PROJECT], p[ID])
        val response = request.execute()
     }
}