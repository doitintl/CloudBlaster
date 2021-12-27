package com.doitintl.blaster.test.tests


import com.doitintl.blaster.shared.runCommand
import com.doitintl.blaster.test.TestBase

class BucketTest(project: String) : TestBase(project) {

    override fun assetTypeIds() =
        listOf("storage.googleapis.com/Bucket")//use same value for both regional and multiregional

    override fun createAssets(sfx: String, project: String): List<String> {
        val multiregionAsset = assetName("bucket-multiregion")
        val regionAsset = assetName("bucket-region")
        makeBucket(project, "us", multiregionAsset)
        makeBucket(project, "us-central1", regionAsset)

        return listOf(multiregionAsset, regionAsset)
    }


}

fun makeBucket(project: String, location: String, bucketName: String) {
    runCommand("gsutil mb -p $project -l $location gs://$bucketName")
    println("Created $bucketName")
}


