package com.doitintl.blaster.deleter

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.container.Container
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.IOException
import java.security.GeneralSecurityException

class GKEClusterDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "location", "id")


    override fun doDelete(p: Map<String?, String?>) {
        val project = p["project"]
        val location = p["location"]
        val id = p["id"]
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val ctnr = Container.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName("application").build()
        val projects = ctnr.projects()
        val clusters = projects.locations().clusters()
        val idTriplet = "projects/$project/locations/$location/clusters/$id"
        val delete = clusters.delete(idTriplet)
        val result = delete.execute()
        println("Deleted Cluster  $id:$result")
    }
}