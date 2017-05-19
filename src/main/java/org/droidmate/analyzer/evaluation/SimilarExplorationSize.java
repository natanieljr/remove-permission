package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.analyzer.exploration.IScenario;

/**
 * Evaluate the scenario on the basis that it did not crash and observed/explored a
 * similar amount of widgets than the initial exploration
 */
@SuppressWarnings("unused")
public class SimilarExplorationSize extends DidNotCrash {
    private IExplorationResult initialExplRes;
    private double threshold;

    SimilarExplorationSize(IScenario groundTruth, double threshold) {
        this.initialExplRes = groundTruth.getResult();
        this.threshold = threshold;

        assert this.threshold > 0;
        assert this.initialExplRes != null;
    }

    @Override
    public double getDissimilarity(IExplorationResult result) {
        assert result != null;

        double groundTruthSize = initialExplRes.getSize();
        double scenarioSize = result.getSize();

        double max = Math.max(groundTruthSize, scenarioSize);

        return Math.abs(groundTruthSize - scenarioSize) / max;
    }

    @Override
    public boolean isValid(IExplorationResult result) {
        return this.getDissimilarity(result) < threshold;
    }
}
