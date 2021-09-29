package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
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
            ).setApplicationName(CLOUD_BLASTER).build()
        }

        fun waitOnOperation(project: String, op: Operation) {
            val engine = getAppEngine()
            val start = currentTimeMillis()
            val threeMin = 1000 * 60 * 3
            val timeout = start + threeMin
            var lastPrint = -1L
            while (currentTimeMillis() < timeout) {
                //operation.name Takes the form apps/myproject/operations/1d7adb9f-79d8-48ed-849f-2454ce417d6f
                val pathParts = op.name.split("/")
                val id = pathParts[pathParts.size - 1]
                val currentOp = engine.apps().operations().get(project, id).execute()!!

                if (currentOp.done != null && currentOp.done) {
                    return
                }
                Thread.sleep(SLEEP_IN_LOOPS_MS)
                if (currentTimeMillis() - lastPrint > 15_000L) {
                    println("Waiting on operation ${op.metadata.get("method")} for ${op.metadata.get("target")}")
                    lastPrint = currentTimeMillis()
                }
            }
            println()
            if (currentTimeMillis() >= timeout) {
                throw TimeoutException("Timed out")
            }
        }
    }
}