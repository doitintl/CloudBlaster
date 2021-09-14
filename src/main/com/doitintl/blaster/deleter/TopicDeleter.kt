package com.doitintl.blaster.deleter

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.TopicName

class TopicDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "id")


    override fun doDelete(p: Map<String?, String?>) {
        TopicAdminClient.create().use {
            val topicName = TopicName.of(p["project"], p["id"])
            it.deleteTopic(topicName)
        }
    }
}