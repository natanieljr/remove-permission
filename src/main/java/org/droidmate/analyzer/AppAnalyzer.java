package org.droidmate.analyzer;

import org.apache.commons.io.FileUtils;
import org.droidmate.analyzer.evaluation.EvaluationStrategyBuilder;
import org.droidmate.analyzer.exploration.DefaultExplorationStrategy;
import org.droidmate.analyzer.exploration.IExplorationStrategy;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

/**
 * Experiment
 */
class AppAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(AppAnalyzer.class);

    private Configuration cfg;

    AppAnalyzer(Configuration cfg) {
        this.cfg = cfg;
    }

    private void initialize() {
        try {
            if (!Files.exists(this.cfg.getWorkDir()))
                Files.createDirectories(this.cfg.getWorkDir());


            FileUtils.cleanDirectory(this.cfg.getWorkDir().toFile());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    void analyze(IAppUnderTest app, ReportGenerator reporter) {
        Date startTime = new Date();
        this.initialize();

        IExplorationStrategy strategy = new DefaultExplorationStrategy(cfg.apiPolicy, cfg.getEvalStrategyBuilder(),
                cfg.getScenarioBuilder());
        app.explore(strategy);

        Date endTime = new Date();
        reporter.addApp(app, startTime, endTime);
    }
}
