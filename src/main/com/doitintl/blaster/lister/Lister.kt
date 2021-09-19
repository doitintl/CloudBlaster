package com.doitintl.blaster.lister

import com.doitintl.blaster.Constants
import com.doitintl.blaster.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.Constants.CLOUD_BLASTER
import picocli.CommandLine
import java.io.FileWriter
import java.util.concurrent.Callable

@CommandLine.Command(
    name = CLOUD_BLASTER,
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Lists assets in a GCP project."]
)
class Lister : Callable<Any> {
    @CommandLine.Option(names = ["-p", "--project"], required = true)
    private lateinit var project: String

    @CommandLine.Option(names = ["-o", "--output"])
    private var outputFile: String = ASSET_LIST_FILE

    @CommandLine.Option(names = ["-f", "--filter-file"])
    private var filterFile: String = Constants.LIST_FILTER_YAML


    @CommandLine.Option(names = ["-n", "--no-filter"])
    private var noFilter: Boolean = false


    override fun call(): Int {
        if (noFilter) {
            println("Outputting all assets, even for types where deletion is not supported, to file $outputFile ")
        }
        FileWritingCallback(outputFile).use { callback ->
            AssetIterator().listAssets(project, callback, noFilter, filterFile)
            return 0
        }
    }


}

internal class FileWritingCallback(filename: String) : Callback<String> {
    private val fw: FileWriter = FileWriter(filename)

    override fun call(s: String) {
        fw.write("$s\n")
    }

    override fun close() {
        fw.close()
    }


}


fun main(args: Array<String>) {
    val exitCode = CommandLine(Lister()).execute(*args)
    if (exitCode != 0) {
        throw RuntimeException("" + exitCode)
    }

}