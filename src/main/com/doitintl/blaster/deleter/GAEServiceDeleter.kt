package com.doitintl.blaster.deleter

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.appengine.v1.Appengine
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.IOException
import java.security.GeneralSecurityException

class GAEServiceDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "id")


    override fun doDelete(p: Map<String?, String?>) {
        val project = p["project"]
        val id = p["id"]
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val engine = Appengine.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName("application").build()
        val services = engine.apps().services()
        val del = services.delete(project, id)
        val result = del.execute()
        println("Deleted GAE Service $id:$result")
    }
}