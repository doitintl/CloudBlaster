package com.doitintl.blaster.deleter

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName

class SubscriptionDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "id")


    override fun doDelete(p: Map<String?, String?>) {
        SubscriptionAdminClient.create()

            .use { subscriptionAdminClient ->
                val subscriptionName = ProjectSubscriptionName.of(p["project"], p["id"])
                subscriptionAdminClient.deleteSubscription(subscriptionName)

            }
    }
}

