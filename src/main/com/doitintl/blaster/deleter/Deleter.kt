package com.doitintl.blaster.deleter

import com.doitintl.blaster.lister.AssetTypeMap
import com.doitintl.blaster.shared.Constants.ALL_ASSETS_ALL_TYPES
import com.doitintl.blaster.shared.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.COMMENT_READY_TO_DELETE
import com.doitintl.blaster.shared.comments
import com.doitintl.blaster.shared.noComment
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
    description = ["Deletes assets listed in file (by default asset-list.txt)"]
)
class Deleter : Callable<Int> {

    @CommandLine.Option(names = ["-d", "--assets-to-delete-file"])
    private var assetsToDeleteFile: String = ASSET_LIST_FILE


    override fun call(): Int {
        val allLines = File(assetsToDeleteFile).readLines()
        if (noComment(allLines).isEmpty()) {
            throw IllegalArgumentException("Nothing to delete")
        }
        if (allLines.any { l -> l.contains(ALL_ASSETS_ALL_TYPES) }) {
            throw IllegalArgumentException("Cannot process a listing of all assets of all types; see supported types in filter file")
        }
        val readyToGo = COMMENT_READY_TO_DELETE.substring(2, COMMENT_READY_TO_DELETE.length)
        val commentsStr = comments(allLines).joinToString("\n")
        if (!commentsStr.toLowerCase().contains(readyToGo.toLowerCase())) {
            throw IllegalArgumentException("Must add \"$readyToGo\" comment to $assetsToDeleteFile to enable deletion.")
        }

        val lines = noComment(allLines)

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

        println("Deleter done: $counter assets")
        return 0
    }


    private fun deleteAsset(line: String) {
        if (line.isBlank()) {
            return
        }
        val deleter = AssetTypeMap().deleterClass(line)

        try {
            deleter.delete(line)
            println("Deleted $line")
        } catch (e: Exception) {
            System.err.println("Error in deleting $line: $e")// Just continue
            e.printStackTrace()
        }
    }
}


fun run(args: Array<String>): Int {
    val exitCode = CommandLine(Deleter()).execute(*args)
    return exitCode
}

fun main(args: Array<String>) {
    val exitCode = run(args)
    exitProcess(exitCode)
}
