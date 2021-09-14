package com.doitintl.blaster.deleter

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.StorageOptions
import java.io.IOException

class BucketDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("id")


    override fun doDelete(p: Map<String?, String?>) {
        val id = p["id"]
        val credentials = GoogleCredentials.getApplicationDefault()
        val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
        val bucket = storage[id] ?: throw IllegalArgumentException("No bucket with id $id")
        var page = bucket.list()
        var counter = 0
        while (true) {
            for (blob in page!!.iterateAll()) {
                counter++
                if (counter % 50 == 0) {
                    println("Deleted $counter blobs from bucket $id")
                }
                blob.delete()
            }
            page = page.nextPage
            if (page == null) {
                break
            }
        }
        bucket.delete()
        println("Deleted bucket $id")
    }
}