package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.appengine.v1.Appengine
import com.google.api.services.appengine.v1.model.Operation
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials


abstract class GAEBaseDeleter : BaseDeleter() {
    companion object {

        fun getAppEngine(): Appengine {
            val credentials = GoogleCredentials.getApplicationDefault()
            val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
            val engine = Appengine.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
            ).setApplicationName(Constants.CLOUD_BLASTER).build()
            return engine
        }

        fun waitOnOperation(project: String, operation: Operation) {
            val engine = getAppEngine()
            while (true) {
                val currentOperation = engine.apps().operations().get(project, operation.name).execute()

                if (currentOperation.done) {
                    return
                }
                Thread.sleep(500)
            }
        }
    }
}