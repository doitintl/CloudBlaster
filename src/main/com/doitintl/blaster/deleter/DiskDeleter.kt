package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class DiskDeleter : GCEAbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/zones/{LOCATION}/disks/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
        val request = computeService.disks().delete(p[PROJECT], p[LOCATION], p[ID])
        val response = request.execute()

    }
}