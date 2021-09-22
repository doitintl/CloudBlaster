package com.doitintl.blaster.test.tests


import com.doitintl.blaster.shared.runCommand
import com.doitintl.blaster.test.TestBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class GAETest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf("srun.googleapis.com/Service")

    override fun createAssets(sfx: String, project: String): List<String> {
        val names = listOf("appengine-standard", "appengine-flex")

        runBlocking {
            names.forEach { s ->
                val service = assetName(s)
                val dir = File("./test-input/$s")
                assert(dir.isDirectory) { "$dir is not directory" }

                launch(Dispatchers.IO) {
                    runCommand("./deploy.sh", dir)
                }
            }
        }



        return names
    }


}


