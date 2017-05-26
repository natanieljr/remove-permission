package org.droidmate.analyzer;

import org.droidmate.analyzer.evaluation.EvaluationStrategyBuilder;
import org.droidmate.analyzer.evaluation.EvaluationType;
import org.droidmate.analyzer.exploration.ScenarioBuilder;
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
    private EvaluationType evaluationType           = EvaluationType.SimilarApis;
    private double evaluationThreshold              = 0.1;
    ApiPolicy apiPolicy                             = ApiPolicy.Mock;
    ScenarioBuilder scenarioBuilder                 = new ScenarioBuilder();
    EvaluationStrategyBuilder evalStrategyBuilder   = new EvaluationStrategyBuilder(this.evaluationType,
            this.evaluationThreshold);

    // Experiment structure
    Path dataDir = Paths.get("data");
    public Path workDir = Paths.get(dataDir.toString(), "tmp");
    public Path extractedResDir = Paths.get("temp_extracted_resources");
}
