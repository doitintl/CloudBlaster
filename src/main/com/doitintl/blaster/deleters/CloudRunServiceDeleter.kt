package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.run.v1.CloudRun
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class CloudRunServiceDeleter : BaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//run.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/services/{ID}")


    override fun doDelete(p: Map<String, String>) {
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val run = CloudRun.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(CLOUD_BLASTER).build()
        val services = run.projects().Locations().services()
        val result = services.delete("projects/${p[PROJECT]}/locations/${p[LOCATION]}/services/${p[ID]}").execute()
    }
}