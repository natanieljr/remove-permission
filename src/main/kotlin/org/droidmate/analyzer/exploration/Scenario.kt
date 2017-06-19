package org.droidmate.analyzer.exploration

import org.apache.commons.io.FileUtils
import org.droidmate.analyzer.Constants
import org.droidmate.analyzer.IAppUnderTest
import org.droidmate.analyzer.api.IApi
import org.droidmate.analyzer.evaluation.IEvaluationStrategy
import org.droidmate.analyzer.wrappers.BoxMateConsts
import org.droidmate.apis.ApiPolicy
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Scenario (to be) explored
 */
class Scenario internal constructor(private val app: IAppUnderTest, override val restrictedApiList: List<IApi>, override val explDepth: Int, private val policy: ApiPolicy,
                                    private val evaluator: IEvaluationStrategy) : IScenario {

    override var result: IExplorationResult? = null
        set(result) {
            val newResDir = this.copyExplOutputToDir(result!!)
            field = ExplorationResult(newResDir)
        }
    private var dir: Path = Constants.EMPTY_PATH
    override var cfgFile: Path = Constants.EMPTY_PATH
        private set(cfgFile) {
            if (cfgFile.parent == this.dir)
                field = cfgFile
            else {

                val fileName = cfgFile.fileName.toString()
                field = this.dir.resolve(fileName)

                try {
                    Files.copy(cfgFile, this.cfgFile, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    logger.error(e.message, e)
                }

            }

            assert(Files.exists(this.cfgFile))
        }
    override var inlinedApk: Path = Constants.EMPTY_PATH
        set(inlinedApk) {
            val fileName = inlinedApk.fileName.toString()
            field = this.dir.resolve(fileName)

            try {
                Files.copy(inlinedApk, this.inlinedApk, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                logger.error(e.message, e)
            }

            assert(Files.exists(this.inlinedApk))
        }

    private fun createDir() {
        try {
            val prefix = String.format("%d_", this.explDepth)

            this.dir = Files.createTempDirectory(this.app.dir, prefix)
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(Files.exists(this.dir))
    }

    private fun createPoliciesFile(restrictedApis: List<IApi>): Path {
        val data = StringBuilder()

        restrictedApis.forEach { p -> data.append(String.format("%s\t%s\n", p.toString(), this.policy.toString())) }

        val res = this.dir.resolve(BoxMateConsts.FILE_API_POLICIES)
        try {
            Files.write(res, data.toString().toByteArray())
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(Files.exists(res))

        return res
    }

    override fun initialize() {
        this.createDir()
        val cfgFile = this.createPoliciesFile(this.restrictedApiList)
        this.cfgFile = cfgFile
    }

    private fun copyExplOutputToDir(res: IExplorationResult): Path {
        val src = res.explDir
        val dst = this.dir.resolve("output_device")

        try {
            if (Files.exists(dst))
                FileUtils.cleanDirectory(dst.toFile())
            Files.deleteIfExists(dst)

            // Depending on the crash, DroidMate does not create the output file
            // Ex:
            // 2017-06-02 16:37:51.690 INFO  org.droidmate.tools.ApksProvider
            //      Reading input apks from /Users/nataniel/Documents/saarland/repositories/remove-permission/data4/tmp
            // 2017-06-02 16:37:51.719 WARN  org.droidmate.android_sdk.Apk
            //      ! While getting metadata for /Users/nataniel/Documents/saarland/repositories/remove-permission/data4/tmp/com.netflix.mediaclient.apk,
            //      got an: org.droidmate.android_sdk.LaunchableActivityNameProblemException: More than one launchable activity found. Returning null apk.
            if (Files.exists(src))
                FileUtils.copyDirectory(src.toFile(), dst.toFile())
            else
                Files.createDirectory(src)

            FileUtils.cleanDirectory(src.toFile())
            Files.deleteIfExists(src)
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(Files.exists(dst))
        return dst
    }

    override val exploredApiList: List<IApi>
        get() {
            if (this.result == null)
                return ArrayList()

            return this.result!!.apiList
        }

    override fun equals(other: Any?): Boolean {
        return other is Scenario && this.restrictedApiList == other.restrictedApiList
    }

    override fun hashCode(): Int {
        var result = app.hashCode()
        result = 31 * result + restrictedApiList.hashCode()
        result = 31 * result + explDepth
        result = 31 * result + policy.hashCode()
        result = 31 * result + evaluator.hashCode()
        result = 31 * result + result.hashCode()
        result = 31 * result + dir.hashCode()
        result = 31 * result + cfgFile.hashCode()
        result = 31 * result + inlinedApk.hashCode()
        return result
    }

    override val isValid: Boolean
        get() = if (this.result == null) false else this.evaluator.isValid(this.result!!)

    override val dissimilarity: Double
        get() = this.evaluator.getDissimilarity(this.result!!)

    companion object {
        private val logger = LoggerFactory.getLogger(Scenario::class.java)
    }

}
