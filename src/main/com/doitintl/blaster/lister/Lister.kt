package com.doitintl.blaster.lister

import com.doitintl.blaster.shared.Constants.ALL_ASSETS_ALL_TYPES_FULL_COMMENT
import com.doitintl.blaster.shared.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.LIST_FILTER_YAML
import com.doitintl.blaster.shared.currentTimeISO
import picocli.CommandLine
import java.io.File
import java.io.FileWriter
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = CLOUD_BLASTER,
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Lists assets in a GCP project."]
)
class Lister : Callable<Any> {
    @CommandLine.Option(names = ["-p", "--project"], required = true)
    private lateinit var project: String

    @CommandLine.Option(names = ["-o", "--output-file"])
    private var outputFile: String = ASSET_LIST_FILE

    @CommandLine.Option(names = ["-f", "--filter-file"])
    private var filterFile: String = LIST_FILTER_YAML


    @CommandLine.Option(names = ["-n", "--no-filter"])
    private var unfiltered: Boolean = false


    override fun call(): Int {
        val outputFile = outputFileName()
        FileWritingCallback(project, outputFile, unfiltered).use { callback ->
            AssetIterator().listAssets(project, callback, unfiltered, filterFile)
            println("Lister done, wrote " + File(outputFile).absolutePath)
            return 0
        }
    }


    private fun outputFileName(): String {
        val isAssetListFileDefault = outputFile == ASSET_LIST_FILE
        return if (unfiltered && isAssetListFileDefault) {
            /* For an unfiltered list, default asset file name is "all-types-asset-list.txt"
             * If the user sets a non-default value, however, we use that.
             */
            allAssetsFileName()
        } else {
            outputFile
        }
    }

    private fun allAssetsFileName(): String {
        val pathParts = outputFile.split(File.separator)
        val newFileName = "all-types-${pathParts.last()}"

        return if (pathParts.size > 1) {
            val dir = pathParts.subList(0, pathParts.lastIndex).joinToString(File.separator)
            dir + File.separator + newFileName
        } else {
            newFileName
        }
    }
}

internal class FileWritingCallback(project: String, filename: String, unfiltered: Boolean) : Callback<String> {
    private val fw: FileWriter = FileWriter(filename)

    init {
        val s = if (unfiltered) {
            "$project at ${currentTimeISO()}; $ALL_ASSETS_ALL_TYPES_FULL_COMMENT"
        } else {
            "$project at ${currentTimeISO()}; Assets after filtering. Review, edit, then add the comment indicating readiness to delete, before passing this to the Deleter"
        }

        fw.write("# $s\n")
    }

    override fun call(s: String) {
        fw.write("$s\n")
    }

    override fun close() {
        fw.close()
    }
}


fun run(args: Array<String>): Int {
    val exitCode = CommandLine(Lister()).execute(*args)
    return exitCode
}

fun main(args: Array<String>) {
    val exitCode = run(args)
    exitProcess(exitCode)
}
