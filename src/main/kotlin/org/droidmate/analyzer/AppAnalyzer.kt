package org.droidmate.analyzer

import org.apache.commons.io.FileUtils
import org.droidmate.analyzer.exploration.DefaultExplorationStrategy
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.util.*

/**
 * Experiment
 */
internal class AppAnalyzer(private val cfg: Configuration) {

    private fun initialize() {
        try {
            if (!Files.exists(this.cfg.workDir))
                Files.createDirectories(this.cfg.workDir)


            FileUtils.cleanDirectory(this.cfg.workDir.toFile())
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

    }

    fun analyze(app: IAppUnderTest, reporter: ReportGenerator) {
        val startTime = Date()
        this.initialize()

        val strategy = DefaultExplorationStrategy(cfg.apiPolicy, cfg.evalStrategyBuilder,
                cfg.scenarioBuilder)
        app.explore(strategy)

        val endTime = Date()
        reporter.addApp(app, startTime, endTime)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AppAnalyzer::class.java)
    }
}
