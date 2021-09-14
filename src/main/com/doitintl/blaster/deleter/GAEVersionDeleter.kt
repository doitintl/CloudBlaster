package com.doitintl.blaster.deleter

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.appengine.v1.Appengine
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class GAEVersionDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "service", "id")


    override fun doDelete(p: Map<String?, String?>) {
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val engine = Appengine.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        )
            //.setApplicationName("application")
            .build()
        val versions = engine.apps().services().versions()
        val del = versions.delete(p["project"], p["service"], p["id"])
        val result = del.execute()

    }
}