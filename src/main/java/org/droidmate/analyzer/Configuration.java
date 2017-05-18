package org.droidmate.analyzer;

import org.droidmate.analyzer.evaluation.DidNotCrash;
import org.droidmate.analyzer.evaluation.EvaluationType;
import org.droidmate.analyzer.evaluation.IEvaluationStrategy;
import org.droidmate.analyzer.evaluation.SimilarApis;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Experiment configuration
 */
public class Configuration {
    // Configuration
    String apkInputDir = "/users/nataniel/Documents/saarland/repositories/remove-permission/apks";

    public EvaluationType evaluationType   = EvaluationType.SimilarApis;
    public double evaluationThreshold      = 0.1;

    // Droidmate related variables
    private Path droidMateDir = Paths.get("/users/nataniel/Documents/saarland/repositories/droidmate/");
    public Path droidMateGradleFileDir = Paths.get(droidMateDir.toString(), "dev/droidmate");
    public Path droidMateMonitoredApis = Paths.get(droidMateGradleFileDir.toString(), "projects/resources/monitored_apis.json");
    public Path droidMateCompiledMonitor = Paths.get(droidMateGradleFileDir.toString(), "projects/monitor-generator/build");
    public Path droidMateMonitorAPKTmp = Paths.get(droidMateCompiledMonitor.toString(), "monitor_api23.apk");
    public Path droidMateMonitorAPK = Paths.get(droidMateGradleFileDir.toString(), "temp_extracted_resources/monitor_api23.apk");

    // Experiment structure
    Path dataDir = Paths.get("data");
    public Path workDir = Paths.get(dataDir.toString(), "tmp");
}
