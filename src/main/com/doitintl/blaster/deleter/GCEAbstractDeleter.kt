package com.doitintl.blaster.deleter

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute

abstract class GCEAbstractDeleter : AbstractDeleter() {

    protected fun createComputeService(): Compute {
        val httpTransport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
        var credential = GoogleCredential.getApplicationDefault()
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        }
        return Compute.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("Google-ComputeSample/0.1")
            .build()
    }
}