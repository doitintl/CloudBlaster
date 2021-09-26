package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.container.Container
import com.google.api.services.container.model.Operation
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.lang.System.currentTimeMillis

class GKEClusterDeleter : BaseDeleter() {
    override val pathPatterns: Array<String>
        get() = arrayOf(
            "//container.googleapis.com/projects/{PROJECT}/zones/{LOCATION}/clusters/{ID}",
            "//container.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/clusters/{ID}"
        )


    override fun doDelete(p: Map<String, String>) {
        val containerApi = getContainerService()
        val clusters = containerApi.projects().locations().clusters()
        val idTriplet = "projects/${p[PROJECT]}/locations/${p[LOCATION]}/clusters/${p[ID]}"
        val delete = clusters.delete(idTriplet)
        val result = delete.execute()

    }


    companion object {
        private fun getContainerService(): Container {
            val requestInitializer = HttpCredentialsAdapter(GoogleCredentials.getApplicationDefault())
            return Container.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), requestInitializer
            ).setApplicationName(CLOUD_BLASTER)
                .build()
        }

        fun waitOnZonalOperation(project: String, location: String, operation: Operation) {
            val start = currentTimeMillis()
            val fourMin = 1000 * 60 * 4
            val timeout = start + fourMin
            while (currentTimeMillis() < timeout) {
                val currentOperation = getContainerService().projects().zones().operations()
                    .get(project, location, operation.name)
                    .execute()

                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(SLEEP_IN_LOOPS_MS)
            }
        }
    }
}