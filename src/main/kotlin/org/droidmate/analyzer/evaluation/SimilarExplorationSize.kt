package org.droidmate.analyzer.evaluation

import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.analyzer.exploration.IScenario

/**
 * Evaluate the scenario on the basis that it did not crash and observed/explored a
 * similar amount of widgets than the initial exploration
 */
class SimilarExplorationSize internal constructor(groundTruth: IScenario, private val threshold: Double) : DidNotCrash() {
    private val initialExplRes: IExplorationResult?

    init {
        this.initialExplRes = groundTruth.result

        assert(this.threshold > 0)
        assert(this.initialExplRes != null)
    }

    override fun getDissimilarity(result: IExplorationResult): Double {
        val groundTruthSize = initialExplRes!!.size
        val scenarioSize = result.size

        val max = Math.max(groundTruthSize, scenarioSize)

        return Math.abs(groundTruthSize - scenarioSize) / max
    }

    override fun isValid(result: IExplorationResult): Boolean {
        return this.getDissimilarity(result) < threshold
    }
}
