package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants
import com.doitintl.blaster.shared.TimeoutException
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.appengine.v1.Appengine
import com.google.api.services.appengine.v1.model.Operation
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.lang.System.currentTimeMillis


abstract class GAEBaseDeleter : BaseDeleter() {
    companion object {

        fun getAppEngine(): Appengine {
            val credentials = GoogleCredentials.getApplicationDefault()
            val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)
            return Appengine.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
            ).setApplicationName(Constants.CLOUD_BLASTER).build()
        }

        fun waitOnOperation(project: String, operation: Operation) {
            val engine = getAppEngine()
            val start = currentTimeMillis()
            val threeMin = 1000 * 60 * 3
            val timeout = start + threeMin

            while (currentTimeMillis() < timeout) {
                //operation.name Takes the form apps/myproject/operations/1d7adb9f-79d8-48ed-849f-2454ce417d6f
                val pathParts = operation.name.split("/")
                val id = pathParts[pathParts.size - 1]
                val currentOp = engine.apps().operations().get(project, id).execute()!!

                if (currentOp.done != null && currentOp.done) {
                    return
                }
                Thread.sleep(SLEEP_IN_LOOPS_MS)
            }
            println()
            if (currentTimeMillis() >= timeout) {
                throw TimeoutException("Timed out")
            }
        }
    }
}