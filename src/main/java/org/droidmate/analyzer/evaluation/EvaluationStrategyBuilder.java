package org.droidmate.analyzer.evaluation;

import org.apache.commons.lang3.NotImplementedException;
import org.droidmate.analyzer.exploration.IScenario;

/**
 * Builder pattern for EvaluationStrategy
 */
public class EvaluationStrategyBuilder {
    private EvaluationType type;
    private double threshold;

    public EvaluationStrategyBuilder(EvaluationType type, double threshold){
        this.type = type;
        this.threshold = threshold;

        assert this.type != null;
    }

    public IEvaluationStrategy build(IScenario initialExpl){
        if (initialExpl == null)
            return new InitialExplStrategy();

        switch (this.type){
            case DidNotCrash:
                return new DidNotCrash();
            case SimilarSize:
                return new SimilarExplorationSize(initialExpl, this.threshold);
            case SimilarApis:
                return new SimilarApis(initialExpl, this.threshold);
            case AlwaysValid:
                return new AlwaysValid(initialExpl, this.threshold);
            default:
                throw new NotImplementedException("Evaluation type required");
        }
    }
}
