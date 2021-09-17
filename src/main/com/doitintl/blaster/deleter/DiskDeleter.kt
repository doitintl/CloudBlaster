package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.doitintl.blaster.shared.Constants.LOCATION

class DiskDeleter : GCEAbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf(PROJECT, LOCATION, ID)


    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
        val request = computeService.disks().delete(p[PROJECT], p[LOCATION], p[ID])
        val response = request.execute()

    }
}