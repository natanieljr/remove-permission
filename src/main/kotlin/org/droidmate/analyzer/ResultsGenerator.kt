package org.droidmate.analyzer

import org.droidmate.analyzer.exploration.ExplorationResult
import org.droidmate.analyzer.exploration.IExplorationResult
import java.nio.file.Files

/**
 * Generate results for the paper
 */
class ResultsGenerator(val cfg : Configuration){
    /*val IExplorationResult.formattedStr
        get() = "$explDir\t${getPolicies()}\t$duration\t${hasCrashed()}\t$nrWidgetsObserved\t$nrWidgetsExplored"*/

    fun generate() {
        assert(Files.exists(cfg.getInputDir()))

        val explResults = this.loadFromDisk()
        val experimentResults = this.initializeExpResults(explResults)

        this.writeResults(experimentResults)
    }

    internal fun initializeExpResults(explResults: List<IExplorationResult>): List<ExperimentResult>{
        val experimentResults : MutableList<ExperimentResult> = ArrayList()

        // Add initial expl
        experimentResults.add(ExperimentResult(ArrayList(), explorationResult = explResults.first()))

        val maxSize = explResults.last().getPolicies().size
        var currSize = 1
        var prevRes = experimentResults
        while (currSize <= maxSize){
            if (explResults.any { p -> p.getPolicies().size == currSize}) {
                val newRes: MutableList<ExperimentResult> = ArrayList()

                explResults
                        .filter { p -> p.getPolicies().size == currSize }
                        .forEach { p -> newRes.add(ExperimentResult(prevRes, p)) }

                experimentResults.addAll(newRes)
                prevRes = newRes
            }
            //val newCurrSize = currSize * 2
            currSize += 1
        }

        return experimentResults
    }

    internal fun loadFromDisk() : List<IExplorationResult>{
        val res : MutableList<IExplorationResult> = ArrayList()

        Files.list(cfg.getInputDir())
                .forEachOrdered{p ->
                    if (Files.isDirectory(p))
                        res.add(ExplorationResult(p, report = true))
                }

        return res
    }

    /*internal fun toString(p : IExplorationResult, iE : IExplorationResult): String{
        return "${p.formattedStr}\t" +
                "${iE.nrWidgetsObserved}\t" +
                "${iE.nrWidgetsExplored}\t" +
                "${this.formattedSimilarSize(p, iE)}\n"
    }*/

    internal fun writeResults(results: List<ExperimentResult>){
        val sb = StringBuilder()

        val header = results.first().getHeader()
        sb.append(header + "\n")

        results
                .subList(1, results.size)
                .asSequence()
                .forEach { p -> sb.append(p.toFmtString()) }

        System.out.println(sb.toString())
    }
}
