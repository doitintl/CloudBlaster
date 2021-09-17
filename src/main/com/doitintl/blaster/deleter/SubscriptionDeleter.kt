package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName

class SubscriptionDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf(PROJECT, ID)


    override fun doDelete(p: Map<String, String>) {
        SubscriptionAdminClient.create()

                .use { subscriptionAdminClient ->
                    val subscriptionName = ProjectSubscriptionName.of(p[PROJECT], p[ID])
                    subscriptionAdminClient.deleteSubscription(subscriptionName)
                }
    }
}

