package com.doitintl.blaster.test.tests


import com.doitintl.blaster.test.TestBase

class CloudFunctionTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "cloudfunctions.googleapis.com/CloudFunction"
    )

    override fun assetNameSeparator(): String {
        return "_"
    }

    override fun createAssets(sfx: String, project: String): List<String> {
        val assetName = assetName("func")
        val workingDir = "cloud-function"
        makeTempFileFromTemplate(assetName, "main.py", workingDir)
        createAssetByRunningDeployScript(assetName, workingDir)
        return listOf(assetName)

    }
}


