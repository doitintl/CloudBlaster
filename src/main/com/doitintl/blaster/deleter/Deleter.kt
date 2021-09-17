package com.doitintl.blaster.deleter

import com.doitintl.blaster.Constants.CLOUD_BLASTER
import com.doitintl.blaster.Constants.LISTED_ASSETS_FILENAME
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
    description = ["Deletes assets listed in assets-to-delete.txt"]
)
class Deleter : Callable<Int> {

    override fun call(): Int {
        val lines = File(LISTED_ASSETS_FILENAME).readLines()
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
        val deleter = AssetTypeMap.instance.deleterClass(line)

        try {
            deleter.delete(line)
            println("Deleted $line")
        } catch (e: Exception) {
            System.err.println("Error in deleting $line: $e")// Just continue
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Deleter()).execute(*args)
            exitProcess(exitCode)
        }
    }
}