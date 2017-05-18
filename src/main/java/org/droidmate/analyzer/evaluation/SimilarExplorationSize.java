package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.analyzer.exploration.IScenario;

/**
 * Evaluate the scenario on the basis that it did not crash and observed/explored a
 * similar amount of widgets than the initial exploration
 */
@SuppressWarnings("unused")
public class SimilarExplorationSize extends DidNotCrash {
    private IExplorationResult groundTruth;
    private double threshold;

    public SimilarExplorationSize(IScenario groundTruth, double threshold) {
        this.groundTruth = groundTruth.getResult();
        this.threshold = threshold;

        assert threshold > 0;
    }

    private double getDifference(IExplorationResult result) {
        double groundTruthSize = groundTruth.getSize();
        double scenarioSize = result.getSize();

        double max = Math.max(groundTruthSize, scenarioSize);

        return Math.abs(groundTruthSize - scenarioSize) / max;
    }

    @Override
    public boolean valid(IExplorationResult result) {
        return this.getDifference(result) < threshold;
    }
}
