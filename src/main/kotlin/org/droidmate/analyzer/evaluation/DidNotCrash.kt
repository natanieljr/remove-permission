package org.droidmate.analyzer.evaluation

import org.droidmate.analyzer.exploration.IExplorationResult

/**
 * Evaluate the scenario on the basis that it did not crash
 */
open class DidNotCrash internal constructor() : IEvaluationStrategy {

    override fun isValid(result: IExplorationResult): Boolean {
        return !result.hasCrashed()
    }

    override fun getDissimilarity(result: IExplorationResult): Double {
        if (this.isValid(result))
            return 0.0
        else
            return 1.0
    }
}
