package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.doitintl.blaster.shared.Constants.ZONE

class GCEInstanceDeleter : GCEAbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf(PROJECT, ZONE, ID)

    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
        val request = computeService.instances().delete(p[PROJECT], p[ZONE], p[ID])
        val response = request.execute()
    }
}