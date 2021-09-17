package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.CLOUD_BLASTER
import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.LOCATION
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.container.Container
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class GKEClusterDeleter : AbstractDeleter() {
    override val pathPatterns: Array<String>
        get() = arrayOf(
            "//container.googleapis.com/projects/{PROJECT}/zones/{LOCATION}/clusters/{ID}",
            "//container.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/clusters/{ID}"
        )


    override fun doDelete(p: Map<String, String>) {
        val requestInitializer = HttpCredentialsAdapter(GoogleCredentials.getApplicationDefault())
        val containerApi = Container.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(CLOUD_BLASTER)
            .build()
        val clusters = containerApi.projects().locations().clusters()
        val idTriplet = "projects/${p[PROJECT]}/locations/${p[LOCATION]}/clusters/${p[ID]}"
        val delete = clusters.delete(idTriplet)
        val result = delete.execute()

    }
}