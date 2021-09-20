package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute
import com.google.api.services.compute.model.Operation


abstract class GCEBaseDeleter : BaseDeleter() {
    companion object {

        fun getComputeService(): Compute {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            var credential = GoogleCredential.getApplicationDefault()
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            }

            return Compute.Builder(httpTransport, jsonFactory, credential).setApplicationName(Constants.CLOUD_BLASTER)
                .build()
        }


        fun waitOnZoneOperation(project: String, location: String, operation: Operation) {
            while (true) {
                val currentOperation: Operation = getComputeService()
                    .zoneOperations()
                    .get(project, location, operation.name)
                    .execute()
                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(500)
            }
        }

        fun waitOnRegionOperation(project: String, location: String, operation: Operation) {
            while (true) {
                val currentOperation: Operation = getComputeService()
                    .regionOperations().get(project, location, operation.name).execute()
                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(500)
            }
        }

        fun waitOnGlobalOperation(project: String, operation: Operation) {
            while (true) {
                val currentOperation: Operation = getComputeService()
                    .globalOperations().get(project, operation.name).execute()
                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(500)
            }
        }
    }
}