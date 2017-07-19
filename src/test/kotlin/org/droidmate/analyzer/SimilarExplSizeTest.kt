package org.droidmate.analyzer

import org.droidmate.analyzer.evaluation.SimilarApis
import org.droidmate.analyzer.exploration.ExplorationResult
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import java.nio.file.Paths

/**
 * Test for the custom exploration strategy
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class SimilarExplSizeTest{
    @Test
    fun calcDissimilarity(){
        val explDir1 = Paths.get("data/codeadore.textgram_3.0.10/0_5215842561409541734")
        val expl1 = ExplorationResult(explDir1, report = true)
        val explDir2 = Paths.get("data/codeadore.textgram_3.0.10/1_473056737438429882")
        val expl2 = ExplorationResult(explDir2, report = true)
        System.out.println("-----")
        System.out.println(expl1.toBracedNotation())
        System.out.println(expl2.toBracedNotation())
        System.out.println("-----")
        val sim = SimilarApis(expl1, 0.5, sortApis = false)
        System.out.println(sim.getDissimilarity(expl2))

        /*System.out.println("-----")
        System.out.println(expl1.toSortedBracedNotation())
        System.out.println(expl2.toSortedBracedNotation())
        System.out.println("-----")
        sim = SimilarApis(expl1, 0.5, sortApis = true)
        System.out.println(sim.getDissimilarity(expl2))*/
    }
}