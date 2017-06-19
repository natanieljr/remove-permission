package org.droidmate.analyzer.evaluation

import org.droidmate.analyzer.exploration.IExplorationResult


/**
 * Interface defining metrics to evaluate scenarios. This evaluation is used to define which
 * scenarios can be combined on @org.droidmate.analyzer.exploration.DefaultExplorationStrategy.generateScenarios
 */
interface IEvaluationStrategy {
    /**
     * Check if a scenario output is valid
     * @param result An exploration result
     * *
     * @return If the scenario is valid
     */
    fun isValid(result: IExplorationResult): Boolean

    fun getDissimilarity(result: IExplorationResult): Double
}
