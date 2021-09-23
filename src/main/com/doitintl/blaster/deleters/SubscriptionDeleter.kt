package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName

class SubscriptionDeleter : BaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//pubsub.googleapis.com/projects/{PROJECT}/subscriptions/{ID}")

    override fun doDelete(p: Map<String, String>) {
        SubscriptionAdminClient.create() .use { subAdminClient ->
                subAdminClient.deleteSubscription(ProjectSubscriptionName.of(p[PROJECT], p[ID]))
            }
    }
}

