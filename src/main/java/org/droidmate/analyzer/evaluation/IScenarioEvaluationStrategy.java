package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.Scenario;

/**
 * Metric to evaluate scenarios
 */
public interface IScenarioEvaluationStrategy {
    boolean valid(Scenario scenario);
}
