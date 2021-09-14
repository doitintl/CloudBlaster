package com.doitintl.blaster

import com.doitintl.blaster.shared.AssetTypeMap
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.LISTED_ASSETS_FILENAME
import picocli.CommandLine
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = CLOUD_BLASTER,
    mixinStandardHelpOptions = true,
    description = ["Deletes assets listed in assets-to-delete.txt"]
)
class Deleter : Callable<Int> {

    override fun call(): Int {
        val br = BufferedReader(FileReader(LISTED_ASSETS_FILENAME))
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