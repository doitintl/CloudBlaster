package com.doitintl.blaster.deleter

class DiskDeleter : GCEAbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "zone", "id")


    override fun doDelete(p: Map<String, String>) {
        val computeService = createComputeService()
        val request = computeService.disks().delete(p["project"], p["zone"], p["id"])
        val response = request.execute()

    }
}