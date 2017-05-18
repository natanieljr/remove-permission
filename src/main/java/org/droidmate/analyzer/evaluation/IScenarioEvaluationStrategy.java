package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IScenario;


/**
 * Interface defining metrics to evaluate scenarios. This evaluation is used to define which
 * scenarios can be combined on @org.droidmate.analyzer.exploration.ExplorationStrategy.generateScenarios
 */
public interface IScenarioEvaluationStrategy {
    /**
     * Check if a scenario output is valid
     * @param scenario A scenario with attached exploration result
     * @return If the scenario is valid
     */
    boolean valid(IScenario scenario);
}
