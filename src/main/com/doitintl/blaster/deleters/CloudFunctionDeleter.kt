package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.CLOUD_BLASTER
import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.LOCATION
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.cloudfunctions.v1.CloudFunctions
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials

class CloudFunctionDeleter : AbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//cloudfunctions.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/functions/{ID}")

    override fun doDelete(p: Map<String, String>) {
        val credentials = GoogleCredentials.getApplicationDefault()
        val funcs = CloudFunctions.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), HttpCredentialsAdapter(credentials)
        ).setApplicationName(CLOUD_BLASTER).build()
        val function = funcs.projects().Locations().functions()
        val idTriplet = "projects/${p[PROJECT]}/locations/${p[LOCATION]}/functions/${p[ID]}"
        val del = function.delete(idTriplet)
        val result = del.execute()

    }
}