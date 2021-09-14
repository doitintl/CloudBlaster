package com.doitintl.blaster

import com.doitintl.blaster.shared.AssetTypeMap
import com.doitintl.blaster.shared.Constants
import picocli.CommandLine
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "checksum",
    mixinStandardHelpOptions = true,
    version = ["0.1"],
    description = ["Cleans up a GCP project."]
)
class Deleter : Callable<Int> {

    override fun call(): Int {
        val br = BufferedReader(FileReader(Constants.LISTED_ASSETS_FILENAME))
        var line: String?
        while (br.readLine().also { line = it } != null) {
            if (line!!.isBlank()) {
                continue
            }
            val assetType = AssetTypeMap.instance.pathToAssetType(line!!)!!
            val deleter = assetType.deleterClass.getConstructor().newInstance()
            deleter.setPathPatterns(assetType.getPathPatterns())
            try {
                deleter.delete(line!!)
                println("Deleted $line")
            } catch (e: Exception) {
                System.err.println("Error in deleting $line:$e")// Just continue
            }
        }
        return 0
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Deleter()).execute(*args)
            exitProcess(exitCode)
        }
    }
}