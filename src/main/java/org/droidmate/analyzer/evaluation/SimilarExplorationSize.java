package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IScenario;

/**
 * Evaluate the scenario on the basis that it did not crash and observed/explored a
 * similar amount of widgets than the initial exploration
 */
@SuppressWarnings("unused")
public class SimilarExplorationSize extends DidNotCrash {
    private IScenario groundTruth;
    private double threshold;

    public SimilarExplorationSize(IScenario groundTruth, double threshold) {
        this.groundTruth = groundTruth;
        this.threshold = threshold;

        assert threshold > 0;
    }

    private double getDifference(IScenario scenario) {
        double groundTruthSize = groundTruth.getSize();
        double scenarioSize = scenario.getSize();

        double max = Math.max(groundTruthSize, scenarioSize);

        return Math.abs(groundTruthSize - scenarioSize) / max;
    }

    @Override
    public boolean valid(IScenario scenario) {
        return this.getDifference(scenario) < threshold;
    }
}
