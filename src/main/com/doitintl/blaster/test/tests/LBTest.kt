package com.doitintl.blaster.test.tests


import com.doitintl.blaster.deleter.GCEBaseDeleter.Companion.getComputeService
import com.doitintl.blaster.deleter.GCEBaseDeleter.Companion.waitOnGlobalOperation
import com.doitintl.blaster.test.TestBase
import com.google.api.services.compute.model.BackendBucket
import com.google.api.services.compute.model.HttpRedirectAction
import com.google.api.services.compute.model.TargetHttpProxy
import com.google.api.services.compute.model.UrlMap


class LBTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "storage.googleapis.com/Bucket",
        "compute.googleapis.com/BackendBucket",
        "compute.googleapis.com/TargetHttpProxy",
        "compute.googleapis.com/UrlMap",
    )

    override fun createAssets(sfx: String, project: String): List<String> {
        val bucketName = assetName("bucketforbackend")//depend on proxy
        val urlMapName = assetName("urlmap")//depend on proxy
        val proxyName = assetName("targethttpproxy")//depend on urlmap
        val backendBucketName = assetName("backendbucket")

        val crossRegionLocation="us"
        val creations = listOf(
            { createBucket(project, crossRegionLocation ,bucketName) },
            { createUrlMap(project, urlMapName) },
            { createTargetProxy(project, proxyName, urlMapName) },
            { createBackendBucket(project, backendBucketName) },
            )

        creations.forEach { creation: () -> Unit ->
            creation()// Must run in order
        }

        val created = listOf(bucketName, urlMapName, proxyName, backendBucketName)

        return created
    }

    private fun createBucket(  project: String,   location:String,       name:String) {
        makeBucket(project, location, name)
    }

    private fun createUrlMap(project: String, name: String) {
        val redirectAction =
            HttpRedirectAction().setHttpsRedirect(true).setRedirectResponseCode("MOVED_PERMANENTLY_DEFAULT")
                .setHostRedirect("example.com").setPathRedirect("/anotherpath")

        val urlMap = UrlMap().setName(name).setDefaultUrlRedirect(redirectAction)
        val operation = getComputeService().urlMaps().insert(project, urlMap).execute()
        waitOnGlobalOperation(project, operation)

    }


    private fun createTargetProxy(project: String, name: String, urlMapName: String) {
        val prox = TargetHttpProxy().setName(name).setUrlMap("projects/${project}/global/urlMaps/${urlMapName}")

        val operation = getComputeService().targetHttpProxies().insert(project, prox).execute()
        waitOnGlobalOperation(project, operation)
    }


    private fun createBackendBucket(project: String, name: String) {
        val backendBucket = BackendBucket().setBucketName(name).setName(name)
        val operation = getComputeService().backendBuckets().insert(project, backendBucket).execute()
        waitOnGlobalOperation(project,  operation)

    }


}




