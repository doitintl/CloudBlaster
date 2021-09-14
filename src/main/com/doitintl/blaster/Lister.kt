package com.doitintl.blaster

import com.doitintl.blaster.shared.AssetIterator
import com.doitintl.blaster.shared.Callback
import com.doitintl.blaster.shared.Constants
import picocli.CommandLine
import java.io.FileWriter
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "checksum",
    mixinStandardHelpOptions = true,
    version = ["0.1"],
    description = ["Cleans up a GCP project."]
)
class Lister : Callable<Any?> {
    @CommandLine.Option(names = ["-p", "--project"], required = true)
    private var project: String? = null

    @CommandLine.Option(names = ["-a", "--print-all-assets"])
    private var allAssetTypes = false


    override fun call(): Int {
        FileWritingCallback(Constants.LISTED_ASSETS_FILENAME).use { callback ->
            val iterator = AssetIterator()
            iterator.listAssets(project, callback, allAssetTypes)
            return 0
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Lister()).execute(*args)
            exitProcess(exitCode)
        }
    }
}

internal class FileWritingCallback(filename: String) : Callback<String> {
    private var fw: FileWriter? = null
    override fun call(s: String) {
        fw!!.write(" $s\n")
    }


    override fun close() {
        fw!!.close()
    }

    init {
        fw = FileWriter(filename)
    }
}