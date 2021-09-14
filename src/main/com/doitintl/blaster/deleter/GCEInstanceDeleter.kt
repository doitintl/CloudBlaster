package com.doitintl.blaster.deleter

class GCEInstanceDeleter : GCEAbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "zone", "id")

    override fun doDelete(p: Map<String?, String?>) {
        try {
            val project = p["project"]
            val id = p["id"]
            val zone = p["zone"]
            val computeService = createComputeService()
            val request = computeService.instances().delete(project, zone, id)
            val response = request.execute()
            println("Deleted instance $id: $response")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}