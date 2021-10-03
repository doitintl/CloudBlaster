package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute
import com.google.api.services.compute.model.Operation
import java.lang.System.currentTimeMillis


abstract class GCEBaseDeleter : BaseDeleter() {

    companion object {
        private const val twoMin = 1000 * 60 * 2


        fun getComputeService(): Compute {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            var credential = GoogleCredential.getApplicationDefault()
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            }

            return Compute.Builder(httpTransport, jsonFactory, credential).setApplicationName(CLOUD_BLASTER)
                .build()
        }


        fun waitOnZonalOperation(
            project: String,
            location: String,
            op: Operation
        ) {
            val start = currentTimeMillis()

            val timeout = start + twoMin
            var lastPrint = -1L
            while (currentTimeMillis() < timeout) {
                if (DONE == getComputeService()
                        .zoneOperations()
                        .get(project, location, op.name)
                        .execute().status
                ) {
                    return
                }
                lastPrint = waiting(lastPrint, start, op)
            }
        }

        private fun waiting(lastPrint: Long, start: Long, op: Operation): Long {
            var ret = lastPrint

            Thread.sleep(SLEEP_IN_LOOPS_MS)
            if (currentTimeMillis() - ret > 15_000L) {
                println("${(currentTimeMillis() - start) / 1000}s waiting on ${op.operationType} for ${op.targetLink}")
                ret = currentTimeMillis()
            }
            return ret
        }

        fun waitOnRegionalOperation(project: String, location: String, op: Operation) {
            val start = currentTimeMillis()
            val timeout = start + twoMin
            var lastPrint = -1L
            while (currentTimeMillis() < timeout) {
                if (DONE == getComputeService().regionOperations().get(project, location, op.name).execute().status) {
                    return
                }
                lastPrint = waiting(lastPrint, start, op)
            }
        }

        fun waitOnGlobalOperation(project: String, op: Operation) {
            val start = currentTimeMillis()

            val timeout = start + twoMin
            var lastPrint = -1L
            while (currentTimeMillis() < timeout) {
                if (DONE == getComputeService()
                        .globalOperations().get(project, op.name).execute().status
                ) {
                    return
                }
                lastPrint = waiting(lastPrint, start, op)

            }
        }
    }
}
