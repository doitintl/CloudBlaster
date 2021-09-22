package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.LOCATION
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.cloud.aiplatform.v1.PipelineServiceClient
import com.google.cloud.aiplatform.v1.PipelineServiceSettings
import com.google.cloud.aiplatform.v1.TrainingPipelineName
import java.util.concurrent.TimeUnit


class TrainingPipelineDeleter : BaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//aiplatform.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/trainingPipelines/{ID}")

    override fun doDelete(p: Map<String, String>) {

        val pipelineServiceSettings: PipelineServiceSettings = PipelineServiceSettings.newBuilder()
            .setEndpoint("${p[LOCATION]}-aiplatform.googleapis.com:443")
            .build()

        PipelineServiceClient.create(pipelineServiceSettings).use { pipelineServiceClient ->
            val trainingPipelineName = TrainingPipelineName.of(p[PROJECT], p[LOCATION], p[ID])
            val future = pipelineServiceClient.deleteTrainingPipelineAsync(trainingPipelineName)
            val result = future.get(300, TimeUnit.SECONDS)
        }
    }
}