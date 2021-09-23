package com.doitintl.blaster.test.tests


import com.doitintl.blaster.deleters.GAEServiceDeleter
import com.doitintl.blaster.shared.runCommand
import com.doitintl.blaster.test.TestBase
import java.io.File
import java.io.FileWriter
import kotlin.system.measureTimeMillis


class GAETest(project: String) : TestBase(project) {
    override fun assetTypeIds(): List<String> = listOf("appengine.googleapis.com/Service")

    override fun createAssets(sfx: String, project: String): List<String> {
        val name = assetName("appengine-standard")
        val dir = File("./test-input/appengine-standard")
        assert(dir.isDirectory) { "$dir is not directory" }

        val template = File(dir, "app.yaml.template").readText()
        val appYamlContent = template.replace("SERVICE", name)
        FileWriter(File(dir, "app.yaml")).use { fw ->
            fw.write(appYamlContent)
        }

        val timeInMillis = measureTimeMillis {
            runCommand("./deploy.sh $project", dir)
        }
        println("Deployed App Engine Service in ${timeInMillis / 1000} s")
        val pattern = GAEServiceDeleter().pathPatterns[0]
        val gaeSvcPath = pattern.replace("{PROJECT}", project).replace("{ID}", name)
        return listOf(gaeSvcPath)
    }

    override fun secondaryAssetsExpected(): Boolean {
        return true
    }

}


