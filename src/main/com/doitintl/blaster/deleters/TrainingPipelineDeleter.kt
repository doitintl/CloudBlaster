package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.LOCATION
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.cloud.aiplatform.v1.PipelineServiceClient
import com.google.cloud.aiplatform.v1.TrainingPipelineName

class TrainingPipelineDeleter : AbstractDeleter() {
    //todo apparently this API, though documented, is unsupported

    override val pathPatterns: Array<String>
        get() = arrayOf("//aiplatform.googleapis.com/projects/{PROJECT}/locations/{LOCATION}/trainingPipelines/{ID}")

    /** Docs in https://github.com/googleapis/java-aiplatform/blob/master/google-cloud-aiplatform/src/main/java/com/google/cloud/aiplatform/v1/PipelineServiceClient.java
     */
    override fun doDelete(p: Map<String, String>) {
        val pipelineServiceClient = PipelineServiceClient.create()

        val name = TrainingPipelineName.of(p.get(PROJECT), p.get(LOCATION), p.get(ID))
        pipelineServiceClient.deleteTrainingPipelineAsync(name).get()

    }
}