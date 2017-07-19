package org.droidmate.analyzer

import org.apache.commons.io.FileUtils
import org.droidmate.analyzer.evaluation.IEvaluationStrategy
import org.droidmate.analyzer.exploration.ScenarioBuilder
import org.droidmate.apis.ApiPolicy
import org.droidmate.analyzer.api.IApi
import org.droidmate.analyzer.exploration.IScenario
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Unit tests for scenario class
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class ScenarioTest{
    fun evalScenario(scenario : IScenario, depth: Int, apiListSize: Int){
        assertTrue(scenario.explDepth == depth)
        assertTrue(scenario.restrictedApiList.size == apiListSize)
        assertTrue(scenario.result == null)
        assertTrue(scenario.cfgFile == Constants.EMPTY_PATH)
        scenario.initialize()
        assertFalse(scenario.cfgFile == Constants.EMPTY_PATH)
    }

    fun createScenario(depth : Int, restrictedApis: List<IApi>) : IScenario{
        val app = mock(IAppUnderTest::class.java)
        `when`(app.dir).thenReturn(Paths.get("test"))
        if (Files.exists(Paths.get("test")))
            FileUtils.deleteDirectory(Paths.get("test").toFile())
        Files.createDirectories(Paths.get("test"))

        val evalStrategy = mock(IEvaluationStrategy::class.java)
        val builder = ScenarioBuilder()

        val scenario = builder.build(app, restrictedApis, depth, ApiPolicy.Deny, evalStrategy)
        evalScenario(scenario, depth, restrictedApis.size)

        return scenario
    }

    @Test
    fun BuildScenarioTest(){
        val restrictedApis = ResourceManager().loadApiMapping("test_list_filled.txt")
        try{
            val scenario0Api = createScenario(0, ArrayList())
            val scenario0Data = Files.readAllLines(scenario0Api.cfgFile)
            scenario0Data.dropLastWhile { p -> p.isEmpty() }
            assertTrue(scenario0Data.isEmpty())

            val scenario1Api = createScenario(1, restrictedApis.subList(0,1))
            val scenario1Data = Files.readAllLines(scenario1Api.cfgFile)
            scenario1Data.dropLastWhile { p -> p.isEmpty() }
            assertFalse(scenario1Data.isEmpty())
            assertTrue(scenario1Data.any { p -> p.contains("Camera->open") })

            val scenario1Api2 = createScenario(1, restrictedApis.subList(1,2))
            val scenario1Data2 = Files.readAllLines(scenario1Api2.cfgFile)
            scenario1Data.dropLastWhile { p -> p.isEmpty() }
            assertFalse(scenario1Data2.isEmpty())
            assertTrue(scenario1Data2.any { p -> p.contains("AudioRecord-><init>") })
        }
        catch(e: IOException){
            Assert.fail(e.message)
        }
    }

    @Test
    fun MergeScenarioTest(){
        val restrictedApis = ResourceManager().loadApiMapping("test_list_filled.txt")
        val scenario1Api = createScenario(1, restrictedApis.subList(0,1))
        val scenario1Api2 = createScenario(1, restrictedApis.subList(1,2))

        val app = mock(IAppUnderTest::class.java)
        `when`(app.dir).thenReturn(Paths.get("test"))
        val evalStrategy = mock(IEvaluationStrategy::class.java)
        val builder = ScenarioBuilder()

        val scenarioMerged = builder.build(scenario1Api, scenario1Api2, app, 2, ApiPolicy.Deny, evalStrategy)
        evalScenario(scenarioMerged, 2, (scenario1Api.restrictedApiList.size + scenario1Api2.restrictedApiList.size))
        val scenarioMergedData = Files.readAllLines(scenarioMerged.cfgFile)
        scenarioMergedData.dropLastWhile { p -> p.isEmpty() }
        assertFalse(scenarioMergedData.isEmpty())
        assertTrue(scenarioMergedData.any { p -> p.contains("Camera->open") })
        assertTrue(scenarioMergedData.any { p -> p.contains("AudioRecord-><init>") })

        val scenarioMerged2 = builder.build(scenarioMerged, scenario1Api2, app, 2, ApiPolicy.Deny, evalStrategy)
        evalScenario(scenarioMerged2, 2, scenarioMerged.restrictedApiList.size)
        val scenarioMerged2Data = Files.readAllLines(scenarioMerged2.cfgFile)
        scenarioMerged2Data.dropLastWhile { p -> p.isEmpty() }
        assertFalse(scenarioMerged2Data.isEmpty())
        assertTrue(scenarioMerged2Data.any { p -> p.contains("Camera->open") })
        assertTrue(scenarioMerged2Data.any { p -> p.contains("AudioRecord-><init>") })

        assertFalse(scenario1Api == scenario1Api2)
        assertTrue(scenarioMerged == scenarioMerged2)
    }
}
