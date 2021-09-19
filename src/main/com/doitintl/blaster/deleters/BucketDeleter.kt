package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageOptions

class BucketDeleter : AbstractDeleter() {
    //todo test on regional and multiregional buckets

    override val pathPatterns: Array<String>
        get() = arrayOf("//storage.googleapis.com/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val id = p[ID]
        val credentials = GoogleCredentials.getApplicationDefault()
        val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
        val bucket = storage[id] ?: throw IllegalArgumentException("Cannot find bucket $id")

        deleteAllBlobsInBucket(bucket)
        bucket.delete()

    }

    private fun deleteAllBlobsInBucket(bucket: Bucket) {
        var page = bucket.list()
        var counter = 0
        while (true) {
            for (blob in page!!.iterateAll()) {
                counter++
                if (counter % 50 == 0) {
                    println("Deleted $counter blobs from bucket ${bucket.name}")
                }
                blob.delete()
            }
            page = page.nextPage
            if (page == null) {
                break
            }
        }
        if (counter > 0) {
            println("Deleted total $counter blobs from bucket ${bucket.name}")
        }
    }
}