package org.droidmate.analyzer.evaluation

import at.unisalzburg.apted.distance.APTED
import at.unisalzburg.apted.node.StringNodeData
import at.unisalzburg.apted.parser.BracketStringInputParser
import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.analyzer.exploration.IScenario

/**
 * Evaluation based on the similarity between the list of explored APIs (Tree edit distance).

 * This method uses the APTED algorihm.
 * Repository was forked to https://github.com/natanieljr/apted and updated.

 * Waiting reply from the author to issue a pull request.

 * Ref: M. Pawlik and N. Augsten. Tree edit distance: Robust and memory- efficient. Information Systems 56. 2016.
 */
open class SimilarApis : IEvaluationStrategy {
    val initialExplRes: IExplorationResult?
    internal val threshold: Double
    internal val sortApis : Boolean

    internal constructor(groundTruth: IScenario, threshold : Double){
        this.initialExplRes = groundTruth.result
        this.threshold = threshold
        this.sortApis = false

        assert(this.threshold > 0)
        assert(this.initialExplRes != null)
    }

    internal constructor(groundTruth: IExplorationResult, threshold: Double, sortApis: Boolean = true){
        this.initialExplRes = groundTruth
        this.threshold = threshold
        this.sortApis = sortApis

        assert(this.threshold > 0)
    }

    override fun getDissimilarity(result: IExplorationResult): Double {
        val parser = BracketStringInputParser()
        val initialExplBracked = if (sortApis) this.initialExplRes!!.toSortedBracedNotation() else this.initialExplRes!!.toBracedNotation()
        val scenarioBracked = if (sortApis) result.toSortedBracedNotation() else result.toBracedNotation()

        val initialExplApis = parser.fromString(initialExplBracked)
        val scenarioApis = parser.fromString(scenarioBracked)

        // Initialise APTED. All operations have cost 1
        val apted = APTED<CustomCostModel, StringNodeData>(CustomCostModel())

        return apted.computeEditDistance(initialExplApis, scenarioApis).toDouble()
    }

    override fun isValid(result: IExplorationResult): Boolean {
        val nrApisInitialExpl = this.initialExplRes!!.apiList.size
        val nrApisScenario = result.apiList.size

        val max = Math.max(nrApisInitialExpl, nrApisScenario).toDouble()
        val normalizedDistance = this.getDissimilarity(result) / max
        return normalizedDistance < this.threshold
    }
}
