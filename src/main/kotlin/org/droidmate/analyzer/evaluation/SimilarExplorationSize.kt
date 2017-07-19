package org.droidmate.analyzer.evaluation

import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.analyzer.exploration.IScenario
import org.slf4j.LoggerFactory

/**
 * Evaluate the scenario on the basis that it did not crash and observed/explored a
 * similar amount of widgets than the initial exploration
 */
class SimilarExplorationSize : IEvaluationStrategy {
    private val initialExplRes: IExplorationResult?
    internal val threshold: Double

    internal constructor(groundTruth: IScenario, threshold : Double){
        this.initialExplRes = groundTruth.result
        this.threshold = threshold

        assert(this.threshold > 0)
        assert(this.initialExplRes != null)
    }

    internal constructor(groundTruth: IExplorationResult, threshold : Double){
        this.initialExplRes = groundTruth
        this.threshold = threshold

        assert(this.threshold > 0)
    }

    override fun getDissimilarity(result: IExplorationResult): Double {
        logger.debug("Processing $this")
        val groundTruthSize = initialExplRes!!.size
        val scenarioSize = result.size

        //val max = Math.max(groundTruthSize, scenarioSize)
        //return Math.abs(groundTruthSize - scenarioSize) / max
        return Math.abs(groundTruthSize - scenarioSize) / groundTruthSize
    }

    override fun isValid(result: IExplorationResult): Boolean {
        return this.getDissimilarity(result) < threshold
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SimilarExplorationSize::class.java)
    }
}
