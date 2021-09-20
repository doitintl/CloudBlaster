package com.doitintl.blaster.deleters

import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class GCEInstanceDeleter : GCEAbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/zones/{LOCATION}/instances/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
        val request = computeService.instances().delete(p[PROJECT], p[LOCATION], p[ID])
        val response = request.execute()
    }
}