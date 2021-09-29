package com.doitintl.blaster.test.tests


import com.doitintl.blaster.shared.runCommand
import com.doitintl.blaster.test.TestBase

class PubSubTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "pubsub.googleapis.com/Subscription",
        "pubsub.googleapis.com/Topic"
    )

    override fun createAssets(sfx: String, project: String): List<String> {
        val sub = assetName("sub")
        val topic = assetName("topic")

        runCommand("gcloud pubsub topics create $topic --project $project")
        println("Created $topic")
        runCommand("gcloud pubsub subscriptions create $sub --topic $topic --project $project")
        println("Created $sub")
        return listOf(sub, topic)
    }


}


