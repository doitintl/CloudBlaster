package com.doitintl.blaster.deleter

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.TopicName

class TopicDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "id")


    override fun doDelete(p: Map<String?, String?>) {
        //todo use use clause here
        val topicAdminClient: TopicAdminClient = TopicAdminClient.create()

        val topicName = TopicName.of(p["project"], p["id"])
        topicAdminClient.deleteTopic(topicName)
        println("Deleted topic ${p["id"]}")

    }
}