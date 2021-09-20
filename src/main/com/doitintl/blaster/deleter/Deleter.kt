package com.doitintl.blaster.deleter

import com.doitintl.blaster.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.Constants.CLOUD_BLASTER
import com.doitintl.blaster.Constants.LIST_FILTER_YAML
import com.doitintl.blaster.lister.AssetTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = CLOUD_BLASTER,
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Deletes assets listed in file (by default asset-list.txt)"]
)
class Deleter : Callable<Int> {

    @CommandLine.Option(names = ["-d", "--assets-to-delete-file"])
    private var assetsToDeleteFile: String = ASSET_LIST_FILE

    @CommandLine.Option(names = ["-f", "--filter-file"])
    private var filterFile: String = LIST_FILTER_YAML

    override fun call(): Int {
        val allLines = File(assetsToDeleteFile).readLines()
        val lines = allLines.filter { l -> !l.trim().startsWith("#") }
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


}


fun main(args: Array<String>) {
    val exitCode = CommandLine(Deleter()).execute(*args)
    if (exitCode != 0) {
        throw RuntimeException("" + exitCode)
    }
}