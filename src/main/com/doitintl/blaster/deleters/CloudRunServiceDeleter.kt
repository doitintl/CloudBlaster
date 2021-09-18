package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants
import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.LOCATION
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.run.v1.CloudRun
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class CloudRunServiceDeleter : AbstractDeleter() {
    //todo test on regional buckets

    override val pathPatterns: Array<String>
        get() = arrayOf("//run.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/services/{ID}")


    override fun doDelete(p: Map<String, String>) {


        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val run = CloudRun.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(Constants.CLOUD_BLASTER).build()
        val services = run.projects().Locations().services()
        val del = services.delete("projects/${p[PROJECT]!!}/locations/${p[LOCATION]!!}/services/${p[ID]!!}")
        val result = del.execute()


    }


}