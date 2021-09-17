package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute

abstract class GCEAbstractDeleter : AbstractDeleter() {

    protected fun createComputeService(): Compute {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        var credential = GoogleCredential.getApplicationDefault()
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        }
        return Compute.Builder(httpTransport, jsonFactory, credential).setApplicationName(CLOUD_BLASTER)
                .build()
    }
}