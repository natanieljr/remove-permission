package org.droidmate.analyzer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
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
    private final String PARAM_INPUT_DIR = "-input";
    private final String PARAM_DEVICE_IDX = "-device";
    private final String PARAM_OUTPUT_DIR = "-output";
    private final String PARAM_EVAL_STRATEGY = "-evalStrategy";
    private final String PARAM_EVAL_THRESHOLD = "-evalThreshold";
    private final String PARAM_API_POLICY = "-apiPolicy";

    // Internal variables
    private ScenarioBuilder scenarioBuilder;
    private EvaluationStrategyBuilder evalStrategyBuilder;
    private Path workDir;
    private Path extractedResDir;

    // Command line parameters
    @Parameter(names = PARAM_INPUT_DIR,
            description = "Path to the input directory, default apks")
    String apkInputDir = "apks";

    @Parameter(names = PARAM_DEVICE_IDX,
            description = "Device index, default 0")
    public int deviceIdx = 0;

    @Parameter(names = PARAM_OUTPUT_DIR, converter = PathConverter.class,
            description = "Output directory, default 'data'")
    Path dataDir = Paths.get("data");

    @Parameter(names = PARAM_EVAL_STRATEGY,
            description = "Evaluation strategy. Supported values: 'DidNotCrash', 'SimilarSize', 'SimilarApis' and 'AlwaysValid'. Default 'AlwaysValid'")
    EvaluationType evaluationType           = EvaluationType.AlwaysValid;

    @Parameter(names = PARAM_EVAL_THRESHOLD,
            description = "Evaluation strategy threshold. Default '0.1'")
    double evaluationThreshold              = 0.1;

    @Parameter(names = PARAM_API_POLICY,
            description = "Api policy. Supported values: 'Allow', 'Deny', 'Mock'. Default 'Mock'")
    ApiPolicy apiPolicy                             = ApiPolicy.Mock;

    // Getters for lazy initialization
    ScenarioBuilder getScenarioBuilder() {
        if (this.scenarioBuilder == null)
            this.scenarioBuilder = new ScenarioBuilder();

        return this.scenarioBuilder;
    }

    EvaluationStrategyBuilder getEvalStrategyBuilder() {
        if (this.evalStrategyBuilder == null)
            this.evalStrategyBuilder = new EvaluationStrategyBuilder(this.evaluationType,
                    this.evaluationThreshold);

        return this.evalStrategyBuilder;
    }

    public Path getWorkDir(){
        if (this.workDir == null)
            this.workDir = dataDir.resolve("tmp");

        return this.workDir;
    }

    public Path getExtractedResDir(){
        if (this.extractedResDir == null) {
            String dir = "temp_extracted_resources";

            if (this.deviceIdx > 0)
                dir += deviceIdx;

            this.extractedResDir = Paths.get(dir);
        }

        return this.extractedResDir;
    }

}
