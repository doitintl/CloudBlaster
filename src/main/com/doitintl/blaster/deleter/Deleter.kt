package com.doitintl.blaster.deleter

import com.doitintl.blaster.Constants.ASSETS_TO_DELETE_DFLT
import com.doitintl.blaster.Constants.CLOUD_BLASTER
import com.doitintl.blaster.Constants.LIST_FILTER_PROPERTIES
import com.doitintl.blaster.lister.AssetTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = CLOUD_BLASTER,
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Deletes assets listed in file (by default assets-to-delete.txt)"]
)
class Deleter : Callable<Int> {

    @CommandLine.Option(names = ["-d", "--assets-to-delete-file"])
    private var assetsToDeleteFile: String = ASSETS_TO_DELETE_DFLT

    @CommandLine.Option(names = ["-f", "--filter-file"])
    private var filterFile: String = LIST_FILTER_PROPERTIES

    override fun call(): Int {
        val lines = File(assetsToDeleteFile).readLines()
        var counter = 0
        runBlocking {
            lines.forEach { line ->
                launch(Dispatchers.IO) {
                    if (line.isNotBlank()) {
                        deleteAsset(line)
                        counter++
                    }
                }
            }
        }

        println("Done: $counter assets")
        return 0
    }

    private fun deleteAsset(line: String) {
        if (line.isBlank()) {
            return
        }
        val deleter = AssetTypeMap(filterFile).deleterClass(line)

        try {
            deleter.delete(line)
            println("Deleted $line")
        } catch (e: Exception) {
            System.err.println("Error in deleting $line: $e")// Just continue
            e.printStackTrace()
        }
    }


        fun main(args: Array<String>) {
            val exitCode = CommandLine(Deleter()).execute(*args)
            exitProcess(exitCode)
        }


}