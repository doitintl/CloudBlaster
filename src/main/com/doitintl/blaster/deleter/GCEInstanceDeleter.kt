package com.doitintl.blaster.deleter

class GCEInstanceDeleter : GCEAbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "zone", "id")

    override fun doDelete(p: Map<String?, String?>) {
        try {
            val computeService = createComputeService()
            val request = computeService.instances().delete(p["project"], p["zone"], p["id"])
            val response = request.execute()
            println("Deleted instance ${p["id"]}: $response")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}