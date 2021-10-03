package com.doitintl.blaster.test.tests


import com.doitintl.blaster.deleters.GAEServiceDeleter
import com.doitintl.blaster.test.TestBase

/**
 * There is no GAEVersionTest.
 * Still, a test could be written that creates a Service and Version
 * deletes the Version, validates that, then deletes the Service in a cleanup step.
 */
class GAEServiceTest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf("appengine.googleapis.com/Service")

    override fun createAssets(sfx: String, project: String): List<String> {

        val assetName = assetName("appengine-std")
        val workingDir = "appengine-standard"
        makeTempFileFromTemplate(assetName, "app.yaml", workingDir)
        createAssetByRunningDeployScript(assetName, workingDir)
        val fullPath = pathForAsset(GAEServiceDeleter().pathPatterns[0], project, assetName)
        //using full path because secondary assets (containers) will be created
        return listOf(fullPath)
    }

    //using full path because secondary assets (containers) will be created
    override fun identifierIsFullPath(): Boolean {
        return true
    }


}


