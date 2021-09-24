package com.doitintl.blaster.test.tests


import com.doitintl.blaster.deleter.GCEBaseDeleter.Companion.waitOnZonalOperation
import com.doitintl.blaster.shared.Constants
import com.doitintl.blaster.test.TestBase
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.container.Container
import com.google.api.services.container.model.Cluster
import com.google.api.services.container.model.CreateClusterRequest
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials


class GKETest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf("container.googleapis.com/Cluster")

    private fun getContainerService(): Container {
        val requestInitializer = HttpCredentialsAdapter(GoogleCredentials.getApplicationDefault())
        return Container.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(Constants.CLOUD_BLASTER).build()
    }

    override fun createAssets(sfx: String, project: String): List<String> {
        val name = assetName("gkecluster")
        val location = "us-central1-c"
        val path = "projects/$project/zones/$location/clusters/$name"

        val cluster = Cluster().setName(name).setInitialNodeCount(1)
        val createClusterReq = CreateClusterRequest().setParent(path).setCluster(cluster)
        val op =
            getContainerService().projects().zones().clusters().create(project, location, createClusterReq).execute()
        waitOnZonalOperation(project, location, op!!)
        return listOf(name)
    }


    override fun waitTimeMillis(): Long {
        val fourMin = 1000L * 60 * 10
        return fourMin
    }
}




