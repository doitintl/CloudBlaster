package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT

class BackendBucketDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/global/backendBuckets/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val project = p[PROJECT]!!
        val id = p[ID]
        val request = getComputeService().backendBuckets().delete(project, id)
        val operation = request.execute()
        waitOnGlobalOperation(project, operation)

    }


}

