package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.doitintl.blaster.shared.Constants.SERVICE
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.appengine.v1.Appengine
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class GAEVersionDeleter : AbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//appengine.googleapis.com/apps/{PROJECT}/services/{SERVICE}/versions/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val engine = Appengine.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(CLOUD_BLASTER).build()
        val versions = engine.apps().services().versions()
        val del = versions.delete(p[PROJECT], p[SERVICE], p[ID])
        val result = del.execute()

    }
}