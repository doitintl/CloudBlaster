package com.doitintl.blaster.test.tests


import com.doitintl.blaster.test.TestBase
import com.google.cloud.aiplatform.v1.LocationName
import com.google.cloud.aiplatform.v1.PipelineServiceClient
import com.google.cloud.aiplatform.v1.PipelineServiceSettings
import com.google.cloud.aiplatform.v1.TrainingPipeline

class TrainingPipelineTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "aiplatform.googleapis.com/TrainingPipeline"
    )

    override fun createAssets(sfx: String, project: String): List<String> {
        val name = assetName("trainingpipeline")
        val location = "us-central1"

        val pipelineServiceSettings: PipelineServiceSettings = PipelineServiceSettings.newBuilder()
            .setEndpoint("$location}-aiplatform.googleapis.com:443").build()

        PipelineServiceClient.create().use { pipelineServiceClient ->
            val parent = LocationName.of(project, location)
            val trainingPipeline = TrainingPipeline.newBuilder().setName(name).build()
            val pipeline = pipelineServiceClient.createTrainingPipeline(parent, trainingPipeline)

        }
        return listOf(name)
    }
}





