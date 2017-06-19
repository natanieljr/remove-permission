package org.droidmate.analyzer.evaluation

import org.apache.commons.lang3.NotImplementedException
import org.droidmate.analyzer.exploration.IScenario

/**
 * Builder pattern for EvaluationStrategy
 */
class EvaluationStrategyBuilder(private val type: EvaluationType?, private val threshold: Double) {

    init {

        assert(this.type != null)
    }

    fun build(initialExpl: IScenario?): IEvaluationStrategy {
        if (initialExpl == null)
            return InitialExplStrategy()

        when (this.type) {
            EvaluationType.DidNotCrash -> return DidNotCrash()
            EvaluationType.SimilarSize -> return SimilarExplorationSize(initialExpl, this.threshold)
            EvaluationType.SimilarApis -> return SimilarApis(initialExpl, this.threshold)
            EvaluationType.AlwaysValid -> return AlwaysValid(initialExpl, this.threshold)
            else -> throw NotImplementedException("Evaluation type required")
        }
    }
}
