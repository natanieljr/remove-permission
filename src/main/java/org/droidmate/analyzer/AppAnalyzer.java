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
            if (!Files.exists(this.cfg.workDir))
                Files.createDirectories(this.cfg.workDir);


            FileUtils.cleanDirectory(this.cfg.workDir.toFile());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    void analyze(IAppUnderTest app, EvaluationStrategyBuilder evaluatorBuilder, ReportGenerator reporter) {
        Date startTime = new Date();
        this.initialize();

        IExplorationStrategy strategy = new DefaultExplorationStrategy(ApiPolicy.Deny, evaluatorBuilder);
        app.explore(strategy);

        Date endTime = new Date();
        reporter.addApp(app, startTime, endTime);
    }
}
