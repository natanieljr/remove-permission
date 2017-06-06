package org.droidmate.analyzer.evaluation;

import at.unisalzburg.apted.costmodel.StringUnitCostModel;
import at.unisalzburg.apted.distance.APTED;
import at.unisalzburg.apted.node.Node;
import at.unisalzburg.apted.node.StringNodeData;
import at.unisalzburg.apted.parser.BracketStringInputParser;
import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.analyzer.exploration.IScenario;
import org.droidmate.analyzer.exploration.Scenario;

/**
 * Evaluation strategy that considers all scenarios valid.
 *
 * Used to perform full exploration
 */
public class AlwaysValid extends SimilarApis {

    AlwaysValid(IScenario initialExpl, double threshold){
        super(initialExpl, threshold);    }

    @Override
    public boolean isValid(IExplorationResult result) {
        return true;
    }
}
