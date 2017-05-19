package org.droidmate.analyzer;

import org.droidmate.analyzer.evaluation.EvaluationType;
import org.droidmate.apis.ApiPolicy;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Experiment configuration
 */
public class Configuration {
    // Input dir
    String apkInputDir = "/users/nataniel/Documents/saarland/repositories/remove-permission/apks";

    // Experiment configuration
    EvaluationType evaluationType   = EvaluationType.SimilarApis;
    double evaluationThreshold      = 0.1;
    ApiPolicy apiPolicy             = ApiPolicy.Mock;

    // DroidMate location
    private Path droidMateDir = Paths.get("/users/nataniel/Documents/saarland/repositories/droidmate/");

    // INternal droidmate related variables
    public Path droidMateGradleFileDir = Paths.get(droidMateDir.toString(), "dev/droidmate");
    public Path droidMateMonitoredApis = Paths.get(droidMateGradleFileDir.toString(), "projects/resources/monitored_apis.json");
    public Path droidMateExtractedRes = Paths.get("temp_extracted_resources");

    // Experiment structure
    Path dataDir = Paths.get("data");
    public Path workDir = Paths.get(dataDir.toString(), "tmp");
}
