package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class AddressDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/regions/{LOCATION}/addresses/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val computeService = getComputeService()
        val operation = computeService.addresses().delete(p[PROJECT]!!, p[LOCATION]!!, p[ID]!!).execute()
        waitOnRegionOperation(p[PROJECT]!!, p[LOCATION]!!, operation)
    }
}