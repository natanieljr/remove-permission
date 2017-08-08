package org.droidmate.analyzer

import at.unisalzburg.apted.costmodel.StringUnitCostModel
import at.unisalzburg.apted.distance.APTED
import at.unisalzburg.apted.node.StringNodeData
import at.unisalzburg.apted.parser.BracketStringInputParser
import com.konradjamrozik.isDirectory
import org.droidmate.analyzer.exploration.ExplorationResult
import org.droidmate.analyzer.exploration.IExplorationResult
import org.droidmate.apis.ApiLogcatMessage
import org.droidmate.report.EventApiPair
import org.droidmate.report.uniqueEventApiPairs
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Test for the custom exploration strategy
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class SimilarTraceTest{

    //region Shared functions

    @Test
    fun dummy(){
        assert(true)
    }

    internal class ReportItem(val initialExpl: Path, val scenario: Path,
                              val eventsInitialExpl: Int, val eventsScenario: Int,
                              val dissimilarity: Double, val dissimilaritySorted: Double,
                              val dissimilaritySanitized: Double, val dissimilaritySanitizedSorted: Double){

        val apiList : MutableList<String> = ArrayList()

        init{
            readApiList()
        }

        internal fun readApiList(){
            val apiListFile = scenario.resolve("api_policies.txt")

            try{
                val apiFileData = Files.readAllLines(apiListFile)
                apiFileData
                        .forEach { p -> apiList.add(p.split("\t")[0]) }
            }
            catch(e: IOException){
                //
            }
        }

        internal fun getFmtApiList(): String{
            val sb = StringBuilder()
            apiList
                    .forEach { p -> sb.append(p).append("\t") }

            return sb.toString()
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(initialExpl.fileName).append("\t")
                    .append(scenario.fileName).append("\t")
                    .append(eventsInitialExpl).append("\t")
                    .append(eventsScenario).append("\t")
                    //.append(dissimilarity).append("\t")
                    //.append(dissimilaritySorted).append("\t")
                    //.append(dissimilaritySanitized).append("\t")
                    //.append(dissimilaritySanitizedSorted).append("\t")
                    .append(getFmtApiList())

            return sb.toString()
        }
    }

    fun toBracedNotation(p: EventApiPair): String{
        val actionStr = p.pair.first
        val api = (p.pair.second as ApiLogcatMessage).api
        val apiStr = api.uniqueString

        return "$actionStr\t$apiStr"
    }

    internal fun toBracedNotation(data: List<String>): String{
        val b = StringBuilder("{root")
        data.forEach{ p -> b.append("{$p}") }
        b.append("}")

        return b.toString()
    }

    internal fun getUniqueEventApiPairs(expl: ExplorationResult, sorted: Boolean): List<String>{
        val data = expl.internalResult.first().actions
                .map{ p -> "${p.base.toShortString()}\tDUMMY" }
                .distinct()

        /*val data = expl.internalResult.first().uniqueEventApiPairs
                .filter { p ->
                    (p.pair.first != "<reset>") &&
                            (p.pair.first != "background") &&
                            (p.pair.first != "unlabeled") &&
                            (!p.pair.first.startsWith("click:[res:id/key_pos_"))
                }
                .map { p -> toBracedNotation(p) }*/

        if (sorted)
            return data
                    .sorted()

        return data
    }

    internal fun compareApiList(expl1: IExplorationResult, expl2: IExplorationResult,
                                sort : Boolean, sanitize: Boolean, apted: Boolean): Triple<Int, Int, Double> {
        if ((expl1 !is ExplorationResult) ||
                (expl2 !is ExplorationResult) ||
                (expl1.internalResult.isEmpty()) ||
                (expl2.internalResult.isEmpty()))
            return Triple(0, 0, -1.0)

        if (apted)
            return compareApiListAPTED(expl1, expl2, sort, sanitize)

        return compareApiListWidgetCoverage(expl1, expl2, sanitize)
    }

    internal fun getInitialExplorationDir(appDir: Path): Path? {
        var maxDir : Path? = null
        var maxVal : Int = 0

        Files
                .list(appDir)
                .filter { p -> p.fileName.toString().startsWith("0_") }
                .findFirst()
                .map { p -> maxDir = p }
                /*.forEach{ p ->
                    if (Files.exists(p.resolve("output_device"))) {
                        val candidate = ExplorationResult(p, true)

                        if (candidate.internalResult.isNotEmpty() &&
                                (candidate.internalResult.first().uniqueEventApiPairs.size > maxVal)){
                            maxVal = candidate.internalResult.first().uniqueEventApiPairs.size
                            maxDir = p
                        }
                    }
                }*/

        return maxDir
    }

    //endregion

    //region APTED

    internal fun calcDissimilarityAPTED(s1: String, s2: String): Double{
        val parser = BracketStringInputParser()

        val s1Fmt = parser.fromString(if (s1.isEmpty()) "{root}" else s1)
        val s2Fmt = parser.fromString(if (s2.isEmpty()) "{root}" else s2)

        // Initialise APTED. All operations have cost 1
        val apted = APTED<StringUnitCostModel, StringNodeData>(StringUnitCostModel())

        return apted.computeEditDistance(s1Fmt, s2Fmt).toDouble()
    }

    internal fun calcDissimilarityAPTED(list1: List<String>, list2: List<String>): Double{
        val s1 = toBracedNotation(list1)
        val s2 = toBracedNotation(list2)

        return calcDissimilarityAPTED(s1, s2)
    }

    internal fun sanitizeApiListAPTED(baseline: List<String>, target: List<String>): List<String>{
        return target
                .filter { p ->
                    baseline.any { q -> q.split("\t")[0].contains(p.split("\t")[0])}
                }
    }

    internal fun compareApiListAPTED(expl1: ExplorationResult, expl2: ExplorationResult,
                                     sort : Boolean, sanitize: Boolean): Triple<Int, Int, Double>{

        val expl1EvtApi = getUniqueEventApiPairs(expl1, sort)
        var expl2EvtApi = getUniqueEventApiPairs(expl2, sort)

        if (sanitize)
            expl2EvtApi = sanitizeApiListAPTED(expl1EvtApi, expl2EvtApi)

        saveTraceAPTED(1, expl2.explDir, expl1EvtApi, sort, false)
        saveTraceAPTED(2, expl2.explDir, expl2EvtApi, sort, sanitize)

        return Triple(expl1EvtApi.size, expl2EvtApi.size, calcDissimilarityAPTED(expl1EvtApi, expl2EvtApi))
    }

    internal fun saveTraceAPTED(explNr: Int, explDir: Path, evts: List<String>, sorted: Boolean, sanitized: Boolean){
        var fileName = "trace_$explNr"

        if (sorted)
            fileName += "_sorted"

        if (sanitized)
            fileName += "_sanitized"

        fileName += ".txt"

        val traceFile = explDir.resolve(fileName)

        val sb = StringBuilder()
        evts.forEach { p -> sb.append("$p \r\n") }

        try {
            Files.write(traceFile, sb.toString().toByteArray())
        } catch(e: IOException) {
            println(e.message)
        }
    }


    //endregion

    //region Widget Coverage

    internal fun sanitizeApiListWidgetCoverage(baseline: List<String>, target: List<String>): List<String>{
        return target
                .filter { p ->
                    baseline.any { q -> q.split("\t")[0].contains(p.split("\t")[0])}
                }
        /*return target
                .filter { p -> baseline.contains(p) }*/
    }

    internal fun groupApiWidgets(evts: List<String>): HashMap<String, MutableList<String>>{
        val res = HashMap<String, MutableList<String>>()

        evts.forEach { evt ->
            val data = evt.split("\t")
            val widget = data[0]
            val api = data[1]

            if (!res.containsKey(widget))
                res.put(widget, ArrayList())

            res[widget]!!.add(api)
        }

        return res
    }

    internal fun calcApiSimilarityWidget(expl1Data: HashMap<String, MutableList<String>>,
                                         expl2Data: HashMap<String, MutableList<String>>,
                                         onlyExisting: Boolean): HashMap<String, Double>{
        val res = HashMap<String, Double>()

        expl1Data.forEach { widget, apis ->
            var apis2 : List<String> = ArrayList()

            if (expl2Data.containsKey(widget))
                apis2 = expl2Data[widget]!!

            val difference = (apis.size - apis2.size) / apis.size.toDouble()

            if (onlyExisting)
                res.put(widget, difference)
        }

        return res
    }

    internal fun compareApiListWidgetCoverage(expl1: ExplorationResult, expl2: ExplorationResult, onlyExisting: Boolean): Triple<Int, Int, Double>{

        val expl1EvtApi = getUniqueEventApiPairs(expl1, true)
        var expl2EvtApi = getUniqueEventApiPairs(expl2, true)
        expl2EvtApi = sanitizeApiListWidgetCoverage(expl1EvtApi, expl2EvtApi)

        val expl1ApisWidget = groupApiWidgets(expl1EvtApi)
        val expl2ApisWidget = groupApiWidgets(expl2EvtApi)

        val diffHash = calcApiSimilarityWidget(expl1ApisWidget, expl2ApisWidget, onlyExisting)

        val avg = diffHash.map { p -> p.value }.sum() / diffHash.size

        saveTraceWidgetCoverage(1, expl2.explDir, expl1ApisWidget)
        saveTraceWidgetCoverage(2, expl2.explDir, expl2ApisWidget)

        return Triple(expl1ApisWidget.size, expl2ApisWidget.size, avg)
    }

    internal fun saveTraceWidgetCoverage(explNr: Int, explDir: Path, widgets: HashMap<String, MutableList<String>>){
        val fileName = "widget_trace_$explNr.txt"
        val traceFile = explDir.resolve(fileName)

        val sb = StringBuilder()
        widgets.forEach { widget, diff ->
            diff.forEach { api -> sb.append("$widget\t$api \r\n") }
        }

        try {
            Files.write(traceFile, sb.toString().toByteArray())
        } catch(e: IOException) {
            println(e.message)
        }

        saveWidgetListListWidgetCoverage(explNr, explDir, widgets)
    }

    internal fun saveWidgetListListWidgetCoverage(explNr: Int, explDir: Path, widgets: HashMap<String, MutableList<String>>){
        val fileName = "widget_list$explNr.txt"
        val traceFile = explDir.resolve(fileName)

        val sb = StringBuilder()
        widgets.forEach { widget, diff -> sb.append("$widget \r\n") }

        try {
            Files.write(traceFile, sb.toString().toByteArray())
        } catch(e: IOException) {
            println(e.message)
        }
    }

    //endregion

    @Test
    fun generateReports(){
        val baseDir = Paths.get("data")
        //val baseDir = Paths.get("/Users/nataniel.borges/Documents/bak/data_full")

        Files.list(baseDir)
                .filter{ appDir -> !appDir.fileName.toString().startsWith("_") }
                .filter{ appDir -> appDir.isDirectory }
                .forEach { appDir ->

            val appResults: MutableList<ReportItem> = ArrayList()

            val initialExplDirCandidate = getInitialExplorationDir(appDir)

            if (initialExplDirCandidate != null) {
                val initialExplDir = initialExplDirCandidate

                if (Files.exists(initialExplDir.resolve("output_device"))) {
                    val initialExpl = ExplorationResult(initialExplDir, true)

                    Files.list(initialExplDir.parent)
                            .filter { p -> p.fileName != initialExplDir.fileName }
                            .forEach { p ->
                                if (p.isDirectory) {
                                    val scenarioDir = p

                                    println("Processing $p")

                                    val scenarioExpl = ExplorationResult(scenarioDir, true)

                                    var dissimilarity = 0.0
                                    var dissimilaritySorted = 0.0
                                    var dissimilaritySanitized = 0.0
                                    var dissimilaritySanitizedSorted = 0.0


                                    val useAPTED = false

                                    var evalRes = compareApiList(initialExpl, scenarioExpl, false, false, apted = useAPTED)
                                    val eventApiInitialExplSize = evalRes.first
                                    val eventApiScenarioSize = evalRes.second
                                    dissimilarity = evalRes.third

                                    if (useAPTED) {
                                        evalRes = compareApiList(initialExpl, scenarioExpl, true, false, apted = useAPTED)
                                        dissimilaritySorted = evalRes.third
                                    }

                                    evalRes = compareApiList(initialExpl, scenarioExpl, false, true, apted = useAPTED)
                                    dissimilaritySanitized = evalRes.third

                                    if (useAPTED) {
                                        evalRes = compareApiList(initialExpl, scenarioExpl, true, true, apted = useAPTED)
                                        dissimilaritySanitizedSorted = evalRes.third
                                    }

                                    val record = ReportItem(initialExplDir, scenarioDir, eventApiInitialExplSize,
                                            eventApiScenarioSize, dissimilarity, dissimilaritySorted,
                                            dissimilaritySanitized, dissimilaritySanitizedSorted)
                                    appResults.add(record)
                                }
                            }

                    val report = appDir.resolve("trace_comparison_report.txt")
                    val sb = StringBuilder()
                    //sb.append("IE\tSCN\tIE APIs\tSCN APIs\tDiff\tSortDiff\tSanDiff\tSortSanDiff \r\n")
                    //sb.append("IE\tSCN\tIE APIs\tSCN APIs\tAPIs \r\n")
                    appResults.forEach { p -> sb.append("$p \r\n") }

                    try {
                        Files.write(report, sb.toString().toByteArray())
                    } catch(e: IOException) {
                        println(e.message)
                    }
                }
            }
        }
    }
}