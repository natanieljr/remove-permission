package org.droidmate.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Perform experiment for multiple applications
 */
class BatchProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessor.class);

    private List<AppUnderTest> apps;
    private Configuration cfg;
    private AppAnalyzer analyzer;

    BatchProcessor(Configuration cfg) {
        this.cfg = cfg;
        this.analyzer = new AppAnalyzer(cfg);
        this.apps = new ArrayList<>();
    }

    private void initializeApkList() {
        Path dirPath = Paths.get(this.cfg.apkInputDir);
        try {
            Stream<Path> files = Files.list(dirPath);
            files.forEachOrdered(apkPath ->
                    {
                        AppUnderTest apk = new AppUnderTest(this.cfg, apkPath);
                        this.apps.add(apk);
                        logger.debug(apk.getPackageName());
                    }
            );
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    void analyze() {
        this.initializeApkList();

        AppUnderTest apk = this.apps.get(0);
        this.analyzer.analyze(apk, cfg.evaluationStrategy);
    }
}
