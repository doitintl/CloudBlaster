package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.LOCATION
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.cloud.aiplatform.v1.PipelineServiceClient
import com.google.cloud.aiplatform.v1.PipelineServiceSettings
import com.google.cloud.aiplatform.v1.TrainingPipelineName


class TrainingPipelineDeleter : AbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//aiplatform.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/trainingPipelines/{ID}")

    /** Docs in https://github.com/googleapis/java-aiplatform/blob/master/google-cloud-aiplatform/src/main/java/com/google/cloud/aiplatform/v1/PipelineServiceClient.java
     */
    override fun doDelete(p: Map<String, String>) {

        //At this stage, the only location is us-central1
        val pipelineServiceSettings: PipelineServiceSettings = PipelineServiceSettings.newBuilder()
                .setEndpoint("${p.get(LOCATION)}-aiplatform.googleapis.com:443")
                .build()

        PipelineServiceClient.create(pipelineServiceSettings).use { pipelineServiceClient ->

            val trainingPipelineName = TrainingPipelineName.of(p.get(PROJECT), p.get(LOCATION), p.get(ID))
            val result = pipelineServiceClient.deleteTrainingPipelineAsync(trainingPipelineName)


        }

    }
}