package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT

import com.google.cloud.logging.v2.MetricsClient
import com.google.logging.v2.LogMetricName


class LogMetricDeleter : BaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//logging.googleapis.com/projects/{PROJECT}/metrics/{ID}")

    override fun doDelete(p: Map<String, String>) {
        MetricsClient.create().use { metricsClient ->
            metricsClient.deleteLogMetric(LogMetricName.of(p[PROJECT], p[ID]))
        }
    }
}