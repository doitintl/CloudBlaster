package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.CLOUD_BLASTER
import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.appengine.v1.Appengine
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class GAEServiceDeleter : AbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//appengine.googleapis.com/apps/{PROJECT}/services/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val engine = Appengine.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(CLOUD_BLASTER).build()
        val services = engine.apps().services()
        val del = services.delete(p[PROJECT], p[ID])
        val result = del.execute()

    }
}