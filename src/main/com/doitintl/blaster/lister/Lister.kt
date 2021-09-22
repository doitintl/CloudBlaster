package com.doitintl.blaster.lister

import com.doitintl.blaster.shared.Constants
import com.doitintl.blaster.shared.Constants.ALL_ASSETS_ALL_TYPES
import com.doitintl.blaster.shared.Constants.ASSET_LIST_FILE
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
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

        FileWritingCallback(outputFile, noFilter).use { callback ->
            AssetIterator().listAssets(project, callback, noFilter, filterFile)
            return 0
        }
    }


}

internal class FileWritingCallback(filename: String, noFilter: Boolean) : Callback<String> {
    private val fw: FileWriter = FileWriter(filename)

    init {
        val s = if (noFilter) {
            ALL_ASSETS_ALL_TYPES
        } else {
            "Assets after filtering. Review, edit, then add the comment indicating readiness to delete, before passing this to the deleter"
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


fun main(args: Array<String>) {
    val exitCode = CommandLine(Lister()).execute(*args)
    if (exitCode != 0) {
        throw RuntimeException("" + exitCode)
    }

}