package com.doitintl.blaster.deleters

import com.doitintl.blaster.Constants.ID
import com.doitintl.blaster.Constants.PROJECT
import com.doitintl.blaster.deleter.AbstractDeleter
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.TopicName

class TopicDeleter : AbstractDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//pubsub.googleapis.com/projects/{PROJECT}/topics/{ID}")

    override fun doDelete(p: Map<String, String>) {
        TopicAdminClient.create().use {
            val topicName = TopicName.of(p[PROJECT], p[ID])
            it.deleteTopic(topicName)
        }
    }
}