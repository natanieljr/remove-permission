package org.droidmate.analyzer

import org.apache.commons.io.FilenameUtils
import org.droidmate.analyzer.exploration.ExplorationResult
import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.analyzer.wrappers.IBoxMateWrapper
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * BoxMate wrapper stub
 */
internal class BoxMateWrapperStub(private val storedExplorationDir: Path) : IBoxMateWrapper{
    fun readApis(dir: Path): List<String> {
        val apiListFile = Files.list(dir)
                .filter{p -> p.fileName.toString().contains("api_policies")}
                .findFirst()

        assert(apiListFile.isPresent)
        return Files.readAllLines(apiListFile.get())
    }

    private fun sameApis(dir: Path, apiList : List<String>) : Boolean{
        try {
            val storedApiList = this.readApis(dir)

            return storedApiList == apiList

            /*var equal = true
            for(storedApi in storedApiList)
                equal = equal && apiList.any { p -> p == storedApi }

            for(seekApi in apiList)
                equal = equal && storedApiList.any { p -> p == seekApi}

            return equal*/
        }
        catch(e: IOException){
            assert(false)
            throw UnsupportedOperationException(e.message)
        }
    }

    private fun locateExploration(apisFile: Path) : Path{
        try {
            val apisFileData = Files.readAllLines(apisFile)

            val expl = Files.list(this.storedExplorationDir)
                    .filter { p -> this.sameApis(p, apisFileData) }
                    .findFirst()

            assert(expl.isPresent)
            return expl.get()
        }
        catch(e: IOException){
            assert(false)
            throw UnsupportedOperationException(e.message)
        }
    }

    override fun inlineApp(apk: Path): Path {
        val inlinedApkFile = Files.list(this.storedExplorationDir)
                .filter{p -> FilenameUtils.getExtension(p.toString()) == "apk"}
                .findFirst()

        assert(inlinedApkFile.isPresent)
        return inlinedApkFile.get()
    }

    override fun explore(apk: Path, policiesFile: Path, isInitialExpl: Boolean): IExplorationResult {
        val expl = this.locateExploration(policiesFile)

        return ExplorationResult(expl)
    }
}
