package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;


/**
 * Interface defining metrics to evaluate scenarios. This evaluation is used to define which
 * scenarios can be combined on @org.droidmate.analyzer.exploration.DefaultExplorationStrategy.generateScenarios
 */
public interface IEvaluationStrategy {
    /**
     * Check if a scenario output is valid
     * @param result An exploration result
     * @return If the scenario is valid
     */
    boolean isValid(IExplorationResult result);

    double getDissimilarity(IExplorationResult result);
}
