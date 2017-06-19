package org.droidmate.analyzer.wrappers

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.droidmate.analyzer.Configuration
import org.droidmate.analyzer.Constants
import org.droidmate.analyzer.exploration.ExplorationResult
import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.android_sdk.AdbWrapper
import org.droidmate.android_sdk.AdbWrapperException
import org.droidmate.configuration.ConfigurationBuilder
import org.droidmate.frontend.DroidmateFrontend
import org.droidmate.misc.SysCmdExecutor
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Wrapper to BoxMate
 */
class BoxMateWrapper(private val cfg: Configuration) {

    private var deviceSN = this.getDeviceSN()

    private fun getDeviceSN(): String {
        val droidmateCfg = ConfigurationBuilder().build(this.getInlineArgs(Constants.EMPTY_PATH).toTypedArray(), FileSystems.getDefault())
        val adbWrapper: AdbWrapper = AdbWrapper(droidmateCfg, SysCmdExecutor())

        try {
            val deviceList = adbWrapper.androidDevicesDescriptors

            if ((this.cfg.deviceIdx < 0) || (this.cfg.deviceIdx > deviceList.size))
                throw AdbWrapperException("Invalid device index.")

            return deviceList[this.cfg.deviceIdx].deviceSerialNumber
        } catch(e: AdbWrapperException) {
            logger.error("No devices found. Proceeding without specifying a device serial number.")
        }

        return "0"
    }

    private fun getExploreArgs(apksDir: Path): List<String> {
        val args = ArrayList<String>()
        args.add(BoxMateConsts.ARGS_API23)
        args.add(BoxMateConsts.ARGS_REPLACE_RESOURCES)
        args.add(BoxMateConsts.ARGS_RESET)
        args.add(BoxMateConsts.ARGS_SEED)
        args.add(BoxMateConsts.ARGS_SNAP)
        args.add(BoxMateConsts.ARGS_TIME)
        args.add(String.format(BoxMateConsts.ARGS_EXPL_OUTPUT_DIR, this.explorationOutputDir.toString()))
        args.add(String.format(BoxMateConsts.ARGS_DEVICE_SEQ, this.cfg.deviceIdx))

        if (this.deviceSN != Constants.EMPTY_DEVICE_SN)
            args.add(String.format(BoxMateConsts.ARGS_DEVICE_SN, this.deviceSN))

        args.add(String.format(BoxMateConsts.ARGS_DIR,
                apksDir.toAbsolutePath().toString()))

        return args
    }

    private fun getInlineArgs(apksDir: Path): List<String> {
        val args = ArrayList<String>()
        args.add(BoxMateConsts.ARGS_INLINE)
        args.add(BoxMateConsts.ARGS_API23)
        args.add(String.format(BoxMateConsts.ARGS_DEVICE_SEQ, this.cfg.deviceIdx))

        if (this.deviceSN != Constants.EMPTY_DEVICE_SN)
            args.add(String.format(BoxMateConsts.ARGS_DEVICE_SN, this.deviceSN))

        args.add(String.format(BoxMateConsts.ARGS_DIR,
                apksDir.toString()))

        return args
    }

    private val explorationOutputDir: Path
        get() = Paths.get("output_device" + this.cfg.deviceIdx)

    private fun copyApkToWorkDir(src: Path): Path {
        val dst = this.cfg.workDir.resolve(src.fileName)

        try {
            Files.copy(src, dst)
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        return dst
    }

    private fun runBoxMate(args: Array<String>) {
        try {
            //int exitCode =
            DroidmateFrontend.main(args, null)
            Thread.sleep(1000)
            //return exitCode;
        } catch (e: Exception) {
            logger.error(e.message, e)
            //return 1;
        }

    }

    private fun findInlinedFile(apk: Path): Path {
        var dst: Path? = null

        val apkFileName = FilenameUtils.removeExtension(apk.fileName.toString())
        try {
            val files = Files.list(this.cfg.workDir)

            val inlinedFile = files.filter { p -> p.fileName.toString().contains(apkFileName) }.findFirst()
            assert(inlinedFile.isPresent)
            assert(Files.exists(inlinedFile.get()))

            dst = inlinedFile.get()
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        return dst!!
    }

    fun inlineApp(apk: Path): Path {
        val fileName = apk.fileName.toString()
        logger.info(String.format("BoxMate inline: %s", fileName))

        try {
            FileUtils.cleanDirectory(this.cfg.workDir.toFile())
            val apkToInline = this.copyApkToWorkDir(apk)

            val args = this.getInlineArgs(apkToInline.toAbsolutePath().parent)
            this.runBoxMate(args.toTypedArray())

            return this.findInlinedFile(apk)
        } catch (e: IOException) {
            logger.error(e.message, e)
            throw UnsupportedOperationException("Could not inline app. Aborting")
        }
    }

    private fun cleanDroidmateDirectories() {
        try {
            val output = this.explorationOutputDir
            if (Files.exists(output)) {
                FileUtils.cleanDirectory(output.toFile())
                Files.delete(output)
            }
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

    }

    private fun deployPoliciesFile(policiesFile: Path) {
        val dst = this.cfg.getExtractedResDir(this.deviceSN).resolve(BoxMateConsts.FILE_API_POLICIES)
        try {
            Files.deleteIfExists(dst)
            assert(!Files.exists(dst))
            Files.copy(policiesFile, dst)
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(Files.exists(dst))
    }

    fun explore(apk: Path, policiesFile: Path, isInitialExpl: Boolean): IExplorationResult {
        val fileName = apk.fileName.toString()
        if (isInitialExpl)
            logger.info(String.format("BoxMate explore: %s", fileName))
        else
            logger.info(String.format("BoxMate explore scenario: %s", apk.fileName.toString()))

        this.cleanDroidmateDirectories()
        this.deployPoliciesFile(policiesFile)

        try {
            FileUtils.cleanDirectory(this.cfg.workDir.toFile())
            val apkToExplore = this.copyApkToWorkDir(apk)

            // Reboot and unlock the device to ensure all tests will be correctly executed
            // Due to exceptions generated form the monitor, sometimes the devices crashes
            //this.adbWrapper.rebootAndUnlock();

            val args = this.getExploreArgs(apkToExplore.parent)
            this.runBoxMate(args.toTypedArray())

            val explDir = this.explorationOutputDir
            return ExplorationResult(explDir)
        } catch (e: IOException) {
            logger.error(e.message, e)
            throw UnsupportedOperationException("Could not explore scenario. Aborting")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoxMateWrapper::class.java)
    }
}
