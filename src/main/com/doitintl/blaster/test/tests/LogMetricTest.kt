package com.doitintl.blaster.test.tests


import com.doitintl.blaster.test.TestBase
import com.google.cloud.logging.v2.MetricsClient
import com.google.logging.v2.LogMetric
import com.google.logging.v2.ProjectName

class LogMetricTest(project: String) : TestBase(project) {
    //todo Apparently metrics are listed only after  a long time, so this test is not worth having
    override fun assetTypeIds(): List<String> = listOf(
        "logging.googleapis.com/LogMetric"
    )//same value twice

    override fun createAssets(sfx: String, project: String): List<String> {
        val metricName = assetName("logmetric")
        MetricsClient.create().use { metricsClient ->
            val parent = ProjectName.of(project)
            val metric = LogMetric.newBuilder().setName(metricName).setFilter("dummy").build()
            val created:LogMetric = metricsClient.createLogMetric(parent, metric)
        }


        return listOf(metricName)
    }


}


