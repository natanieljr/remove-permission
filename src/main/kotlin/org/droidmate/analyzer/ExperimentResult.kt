package org.droidmate.analyzer

import org.droidmate.analyzer.evaluation.DidNotCrash
import org.droidmate.analyzer.evaluation.SimilarExplorationSize
import org.droidmate.analyzer.exploration.IExplorationResult

/**
 * Experiment result structure
 */
class ExperimentResult(private val previousResults: List<ExperimentResult>,
                       private val explorationResult: IExplorationResult) {

    val initialExpl: IExplorationResult?
        get() {
            if (previousResults.isNotEmpty())
                return previousResults[0].explorationResult

            return null
        }

    internal fun equivDidNotCrash(): Pair<Double, Boolean> {
        val res = DidNotCrash()
        val dissimilarity = res.getDissimilarity(this.explorationResult)
        val equiv = res.isValid(this.explorationResult)

        return Pair(dissimilarity, equiv)
    }

    fun didNotCrashFmt(): String {
        val resDidNotCrash = this.equivDidNotCrash()

        return "${resDidNotCrash.first}\t${resDidNotCrash.second}"
    }

    internal fun dissimilaritySimilarSize(): Double {
        val dissimilarity = SimilarExplorationSize(this.initialExpl!!, 0.0)
                .getDissimilarity(this.explorationResult)

        return dissimilarity
    }

    internal fun equivSimilarSize(): List<Boolean> {
        val equivList: MutableList<Boolean> = ArrayList()

        arrayOf(0.05, 0.10, 0.15, 0.20, 0.30, 0.40, 0.50)
                .forEach { threshold ->
                    val res = SimilarExplorationSize(this.initialExpl!!, threshold)
                    res.getDissimilarity(this.explorationResult)
                    val equiv = res.isValid(this.explorationResult)

                    equivList.add(equiv)
                }

        return equivSimilarSize()
    }

    fun similarSizeFmt(): String {
        val dissimilarity = this.dissimilaritySimilarSize()
        val equiv = this.equivSimilarSize()

        val sb = StringBuilder()
        equiv.forEach { p -> sb.append("\t$p") }

        return "$dissimilarity$sb"
    }

    fun getHeader(): String {
        val sb = StringBuilder()
        sb.append("\t\t\t\t\tDid Not Crash\t\tSimilar Size\t\t\t\t\t\t \n")
        sb.append("Directory\tRestricted\tDuration\tIE Size\tSize\tDist\tEquiv\tDist\t0.05\t0.10\t0.15\t0.20\t0.30\t0.40\t0.50 \n")

        return sb.toString()
    }

    fun toFmtString(): String{
        if (this.initialExpl != null) {

            val size = this.explorationResult.size
            val didNotCrash = this.didNotCrashFmt()
            val similarSize = this.similarSizeFmt()

            return "$size\t$didNotCrash\t$similarSize"
        }

        return ""
    }
}