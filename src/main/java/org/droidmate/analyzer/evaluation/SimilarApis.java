package org.droidmate.analyzer.evaluation;

import at.unisalzburg.apted.distance.APTED;
import at.unisalzburg.apted.node.Node;
import at.unisalzburg.apted.node.StringNodeData;
import at.unisalzburg.apted.parser.BracketStringInputParser;
import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.analyzer.exploration.IScenario;

/**
 * Evaluation based on the similarity between the list of explored APIs (Tree edit distance).
 *
 * This method uses the APTED algorihm.
 * Repository was forked to https://github.com/natanieljr/apted and updated.
 *
 * Waiting reply from the author to issue a pull request.
 *
 * Ref: M. Pawlik and N. Augsten. Tree edit distance: Robust and memory- efficient. Information Systems 56. 2016.
 */
public class SimilarApis implements IEvaluationStrategy {
    private double threshold;
    private IExplorationResult initialExplRes;

    SimilarApis(IScenario initialExpl, double threshold){
        this.initialExplRes = initialExpl.getResult();
        this.threshold = threshold;

        assert this.threshold > 0;
        assert this.initialExplRes != null;
    }

    @Override
    public double getDissimilarity(IExplorationResult result){
        assert result != null;

        BracketStringInputParser parser = new BracketStringInputParser();
        String initialExplBracked = this.initialExplRes.toBrackedNotation();
        String scenarioBracked = result.toBrackedNotation();

        Node<StringNodeData> initialExplApis = parser.fromString(initialExplBracked);
        Node<StringNodeData> scenarioApis = parser.fromString(scenarioBracked);

        // Initialise APTED. All operations have cost 1
        APTED<CustomCostModel, StringNodeData> apted = new APTED<>(new CustomCostModel());

        return apted.computeEditDistance(initialExplApis, scenarioApis);
    }

    @Override
    public boolean isValid(IExplorationResult result) {
        int nrApisInitialExpl = this.initialExplRes.getApiList().size();
        int nrApisScenario = result.getApiList().size();

        double max = Math.max(nrApisInitialExpl, nrApisScenario);
        double normalizedDistance = this.getDissimilarity(result)/max;
        return normalizedDistance < this.threshold;
    }
}
