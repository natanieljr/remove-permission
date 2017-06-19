package org.droidmate.analyzer

import com.beust.jcommander.Parameter
import com.beust.jcommander.converters.PathConverter
import org.droidmate.analyzer.evaluation.EvaluationStrategyBuilder
import org.droidmate.analyzer.evaluation.EvaluationType
import org.droidmate.analyzer.exploration.ScenarioBuilder
import org.droidmate.apis.ApiPolicy

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Experiment configuration
 */
class Configuration {
    // Command line parameters
    @Parameter(names = arrayOf("-input"), description = "Path to the input directory, default apks")
    internal var apkInputDir = "apks"

    @Parameter(names = arrayOf("-device"), description = "Device index, default 0")
    var deviceIdx = 0

    @Parameter(names = arrayOf("-output"), converter = PathConverter::class, description = "Output directory, default 'data'")
    internal var dataDir = Paths.get("data")

    @Parameter(names = arrayOf("-evalStrategy"), description = "Evaluation strategy. Supported values: 'DidNotCrash', 'SimilarSize', 'SimilarApis' and 'AlwaysValid'. Default 'AlwaysValid'")
    internal var evaluationType = EvaluationType.AlwaysValid

    @Parameter(names = arrayOf("-evalThreshold"), description = "Evaluation strategy threshold. Default '0.1'")
    internal var evaluationThreshold = 0.1

    @Parameter(names = arrayOf("-apiPolicy"), description = "Api policy. Supported values: 'Allow', 'Deny', 'Mock'. Default 'Mock'")
    internal var apiPolicy = ApiPolicy.Mock

    // Internal variables
    internal val scenarioBuilder: ScenarioBuilder = ScenarioBuilder()

    internal var evalStrategyBuilder: EvaluationStrategyBuilder = EvaluationStrategyBuilder(this.evaluationType,
            this.evaluationThreshold)

    internal val workDir: Path = dataDir.resolve("tmp")

    internal fun getExtractedResDir(deviceSN: String): Path {
        return Paths.get("temp_extracted_resources$deviceSN")
    }

}
