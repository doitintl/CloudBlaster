package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.TopicName

class TopicDeleter : BaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//pubsub.googleapis.com/projects/{PROJECT}/topics/{ID}")

    override fun doDelete(p: Map<String, String>) {
        TopicAdminClient.create().use {
            it.deleteTopic(TopicName.of(p[PROJECT], p[ID]))
        }
    }
}