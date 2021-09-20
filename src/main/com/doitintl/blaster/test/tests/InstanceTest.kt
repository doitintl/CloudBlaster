package com.doitintl.blaster.test.tests


import com.doitintl.blaster.shared.runCommand
import com.doitintl.blaster.test.TestBase

/**
 * This test generates exception messages on an attempt to delete  the attached disk. But that does not affect functionality.
 */
class InstanceTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf("compute.googleapis.com/Disk", "compute.googleapis.com/Instance")

    override fun createAssets(sfx: String, project: String): List<String> {
        val instance = assetName("instance")


        runCommand("gcloud compute instances create --zone us-central1-a $instance --project $project")
        return listOf(instance)
    }


}


