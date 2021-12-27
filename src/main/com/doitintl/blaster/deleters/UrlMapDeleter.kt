package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.GCEBaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.api.services.compute.Compute

class UrlMapDeleter : GCEBaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//compute.googleapis.com/projects/{PROJECT}/global/urlMaps/{ID}")



    override fun doDelete(p: Map<String, String>) {
        val project = p[PROJECT]!!
        val id = p[ID]
        val request = getComputeService().urlMaps().delete(project,  id)
        val operation = request.execute();
        waitOnGlobalOperation(project, operation)
    }


}

