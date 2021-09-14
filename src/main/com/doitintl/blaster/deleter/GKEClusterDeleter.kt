package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.container.Container
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class GKEClusterDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "location", "id")


    override fun doDelete(p: Map<String?, String?>) {
        val requestInitializer = HttpCredentialsAdapter(GoogleCredentials.getApplicationDefault())
        val containerApi = Container.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(Constants.CLOUD_BLASTER)
            .build()
        val projects = containerApi.projects()
        val clusters = projects.locations().clusters()
        val idTriplet = "projects/${p["project"]}/locations/${p["location"]}/clusters/${p["id"]}"
        val delete = clusters.delete(idTriplet)
        val result = delete.execute()

    }
}