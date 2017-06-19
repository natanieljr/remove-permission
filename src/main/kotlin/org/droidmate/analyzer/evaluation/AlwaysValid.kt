package org.droidmate.analyzer.evaluation

import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.analyzer.exploration.IScenario

/**
 * Evaluation strategy that considers all scenarios valid.

 * Used to perform full exploration
 */
class AlwaysValid internal constructor(initialExpl: IScenario, threshold: Double) : SimilarApis(initialExpl, threshold) {

    override fun isValid(result: IExplorationResult): Boolean {
        return true
    }
}
