package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.ID
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.StorageOptions

class BucketDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf(ID)


    override fun doDelete(p: Map<String, String>) {
        val id = p[ID]
        val credentials = GoogleCredentials.getApplicationDefault()
        val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
        val bucket = storage[id]!!
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
        if (counter > 0) {
            println("Deleted total $counter blobs from bucket $id")
        }
        bucket.delete()

    }
}