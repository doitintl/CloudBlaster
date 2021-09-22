package com.doitintl.blaster.test.tests


import com.doitintl.blaster.shared.runCommand
import com.doitintl.blaster.test.TestBase
import java.io.File

class CloudRunTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "appengine.googleapis.com/Service"
    )//same value twice

    override fun createAssets(sfx: String, project: String): List<String> {
        val cloudRunService = assetName("cloudrunsvc")
        val dir = File("./test-input/cloud-run")
        assert((dir).isDirectory) { "$dir is not directory" }
        runCommand("./deploy.sh", dir)
        return listOf(cloudRunService)
    }


}


