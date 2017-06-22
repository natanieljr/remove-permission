package org.droidmate.analyzer

import org.droidmate.analyzer.exploration.GenerateAllExplorationStrategy
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Experiment
 */
internal class AppAnalyzer(private val cfg: Configuration) {

    fun analyze(app: IAppUnderTest, reporter: ReportGenerator) {
        logger.info(String.format("Starting anaylsis of app: %s", app.toString()))
        val startTime = Date()

        val strategy = GenerateAllExplorationStrategy(cfg.apiPolicy, cfg.evalStrategyBuilder,
                cfg.scenarioBuilder)
        app.explore(strategy)

        val endTime = Date()
        reporter.addApp(app, startTime, endTime)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AppAnalyzer::class.java)
    }
}
