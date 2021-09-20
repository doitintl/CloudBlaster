package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.AbstractDeleter
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.logging.v2.Logging
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials


class LogMetricDeleter : AbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//logging.googleapis.com/projects/{PROJECT}/metrics/{ID}")

    override fun doDelete(p: Map<String, String>) {
        val project = p[PROJECT]!!
        val id = p[ID]!!
        val credentials = GoogleCredentials.getApplicationDefault()
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
        val logging = Logging.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
        ).setApplicationName(CLOUD_BLASTER).build()
        val metrics = logging.Projects().metrics()
        val del = metrics.delete("projects/$project/metrics/$id")
        val result = del.execute()

    }
}