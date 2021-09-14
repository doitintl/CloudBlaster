package com.doitintl.blaster.deleter

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName

class SubscriptionDeleter : AbstractDeleter() {
    override val pathKeys: Array<String>
        get() = arrayOf("project", "id")


    override fun doDelete(p: Map<String?, String?>) {
        val subscriptionAdminClient = SubscriptionAdminClient.create()

        val subscriptionName = ProjectSubscriptionName.of(p["project"], p["id"])
        subscriptionAdminClient.deleteSubscription(subscriptionName)
        println("Deleted subscription $subscriptionName")
    }
}