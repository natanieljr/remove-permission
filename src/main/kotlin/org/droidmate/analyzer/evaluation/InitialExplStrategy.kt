package org.droidmate.analyzer.evaluation

import org.droidmate.analyzer.exploration.IExplorationResult

/**
 * Validation for the initial exploration, just check if it crashed or not
 */
class InitialExplStrategy internal constructor() : IEvaluationStrategy {

    override fun isValid(result: IExplorationResult): Boolean {
        return !result.hasCrashed()
    }

    override fun getDissimilarity(result: IExplorationResult): Double {
        return 0.0
    }
}
