package com.doitintl.blaster.test.tests


import com.doitintl.blaster.test.TestBase

class CloudRunTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "run.googleapis.com/Service"
    )

    override fun createAssets(sfx: String, project: String): List<String> {
        val assetName = assetName("cloudrun")
        createAssetByRunningDeployScript(assetName, "cloud-run")
        return listOf(assetName)
    }


}


