package org.droidmate.analyzer

import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Perform experiment for multiple applications
 */
internal class BatchProcessor(private val cfg: Configuration) {

    private val apps: MutableList<IAppUnderTest>
    private val analyzer: AppAnalyzer = AppAnalyzer(cfg)

    init {
        this.apps = ArrayList<IAppUnderTest>()
    }

    private fun initializeApkList() {
        val dirPath = Paths.get(this.cfg.inputDir)
        try {
            val files = Files.list(dirPath)
            files.filter { p -> p.fileName.toString().contains(".apk") }
                    .forEachOrdered { apkPath ->
                        val apk = AppUnderTest(this.cfg, apkPath)
                        this.apps.add(apk)
                        logger.debug(apk.packageName)
                    }
        } catch (e: IOException) {
            logger.error(e.message, e)
        }
    }

    fun analyze() {
        this.initializeApkList()

        val reporter = ReportGenerator()

        this.apps.forEach { apk ->
            logger.info(String.format("Executing app %s", apk.toString()))
            this.analyzer.analyze(apk, reporter)
            reporter.generateReport()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BatchProcessor::class.java)
    }
}
