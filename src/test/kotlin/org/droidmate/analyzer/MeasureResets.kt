package org.droidmate.analyzer

import com.konradjamrozik.isDirectory
import org.droidmate.exploration.actions.ResetAppExplorationAction
import org.droidmate.exploration.actions.WidgetExplorationAction
import org.droidmate.report.OutputDir
import org.droidmate.report.uniqueString
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class MeasureResets{
    private fun groupAndPrint(scenario: Path, l: List<String>, sb: StringBuilder){
        val hash = HashMap<String, Int>()

        l.forEach { p ->
            if (!hash.containsKey(p)){
                hash.put(p, 0)
            }

            val v = hash[p]!! + 1
            hash.put(p, v)
        }

        hash.forEach { widget, count ->
            sb.appendln("${scenario.parent.fileName}\t${scenario.fileName}\t$count\t$widget")
        }
    }

    private fun getResetActionsIE(scenario: Path, sb: StringBuilder): List<String>{
        val outputDir = scenario.resolve("output_device")
        val explorationResult = OutputDir(outputDir).explorationOutput2

        var lastRest = 1

        val res : MutableList<String> = ArrayList()
        val actionList = explorationResult.first().actions
        actionList.forEachIndexed { index, action ->
            if (action.base is ResetAppExplorationAction) {
                // Didn't do 30 steps
                if ( ((index - lastRest) < 30) && (index > 1)) {
                    val previousAction = actionList[index-1].base

                    // Previous was click on some widget
                    if (previousAction is WidgetExplorationAction) {
                        // Was not on the keyboard
                        if (!previousAction.widget.uniqueString.contains("com.google.android.inputmethod.latin")) {
                            // Is not included in the initial exploration
                            res.add(previousAction.widget.uniqueString)
                            //sb.appendln("${scenario.parent.fileName}\t${scenario.fileName}\t$index\t${previousAction.widget.uniqueString}")
                        }
                    }
                    else {
                        println(previousAction.javaClass.toString())
                    }
                }

                lastRest = index
            }
        }

        println("Finished ${scenario.parent.fileName}\t${scenario.fileName}\t$scenario")
        groupAndPrint(scenario, res, sb)

        return res
    }

    private fun getRestActionsNotSeen(scenario: Path, sb: StringBuilder, ie: List<String>): List<String>{
        val outputDir = scenario.resolve("output_device")
        val explorationResult = OutputDir(outputDir).explorationOutput2

        val res : MutableList<String> = ArrayList()
        res.addAll(ie)

        val actionList = explorationResult.first().actions
        actionList.forEachIndexed { index, action ->
            if (action is WidgetExplorationAction) {
                if (!action.widget.uniqueString.contains("com.google.android.inputmethod.latin")) {
                    if (res.contains(action.widget.uniqueString))
                        res.remove(action.widget.uniqueString)
                }
            }
        }

        groupAndPrint(scenario, res, sb)
        println("Finished ${scenario.parent.fileName}\t${scenario.fileName}\t$scenario")

        return res
    }

    @Test
    fun CalculateNrRests() {
        val baseDir = Paths.get("bak/data_full")

        val sb = StringBuilder()

        Files.list(baseDir)
                .filter { appDir -> !appDir.fileName.toString().startsWith("bak") }
                .filter { appDir -> !appDir.fileName.toString().startsWith("_") }
                .filter { appDir -> appDir.isDirectory }
                .forEach { appDir ->
                    val ieDir = Files.list(appDir)
                            .filter { scenario -> scenario.fileName.toString().startsWith("0_") }
                            .findFirst()

                    //val initialRes =
                    val ieRes = getResetActionsIE(ieDir.get(), sb)

                    Files.list(appDir)
                            .filter { scenario -> !scenario.fileName.toString().startsWith("0_") }
                            .filter { scenario -> scenario.isDirectory }
                            .forEach { scenario ->
                                //val scenarioRes =
                                getRestActionsNotSeen(scenario, sb, ieRes)
                            }
                }

        println(sb.toString())
    }
}