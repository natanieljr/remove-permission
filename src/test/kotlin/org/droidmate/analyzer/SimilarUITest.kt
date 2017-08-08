package org.droidmate.analyzer

import at.unisalzburg.apted.distance.APTED
import at.unisalzburg.apted.node.StringNodeData
import at.unisalzburg.apted.parser.BracketStringInputParser
import com.konradjamrozik.isDirectory
import org.droidmate.analyzer.evaluation.CustomCostModel
import org.droidmate.analyzer.exploration.ExplorationResult
import org.droidmate.device.datatypes.UiautomatorWindowDump
import org.droidmate.device.datatypes.Widget
import org.droidmate.errors.ForbiddenOperationError
import org.droidmate.exploration.actions.RunnableExplorationActionWithResult
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import java.awt.Dimension
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.util.ArrayList

/**
 * Unit tests for scenario class
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class SimilarUITest {
    internal class Screen(val status: String, val idx: Int, val nrOrigWidgets: Int, val nrWidgets: Int,
                          val nrMissingWidgets: Int, val nrNewWidgets: Int, val loss: Double){
        override fun toString(): String{
            return "$status Screen $idx\t IEX size: $nrOrigWidgets\t SCN size: $nrWidgets\t IEX only: $nrMissingWidgets\t SCN only: $nrNewWidgets\t Loss: $loss\r\n"
        }
    }
    internal class Result(val scenarioDir: Path, val screens : List<Screen>){

        internal fun calcResult(): Double{
            var totalLoss = 0.0
            var totalSize = 0.0

            this.screens.forEach { p ->
                totalLoss += p.nrMissingWidgets
                totalSize += p.nrOrigWidgets
            }

            return totalLoss / totalSize
        }

        internal fun getPolicies(): String{
            try{
                val data = Files.readAllLines(this.scenarioDir.resolve("api_policies.txt"))

                val sb = StringBuilder()
                data.forEach { p ->
                    if (p.contains("\t")){
                        sb.append(p.split("\t").get(0) + "\r\n")
                    }
                }

                return sb.toString()
            }
            catch(e: IOException){
                return ""
            }
        }

        override fun toString(): String {
            val sb = StringBuilder()

            val app = this.scenarioDir.parent.fileName.toString()
            val scenario = this.scenarioDir.fileName.toString()
            val policies = this.getPolicies()

            sb.append("App: $app\r\n")
            sb.append("Scenario: $scenario\r\n")
            sb.append("Policies: $policies\r\n")

            this.screens.forEach { p -> sb.append(p.toString()) }

            sb.append("* TOTAL: ${this.calcResult()}\r\n\r\n")

            return sb.toString()
        }
    }

    internal class Node(val value : Widget, val children : MutableList<Node>){
        fun Widget.toFmtString(): String{
            val identifier : String

            if (this.id != null && this.id.isNotEmpty())
                identifier = this.id
            /*else if (this.text.isNotEmpty())
                identifier = this.text
            else if (this.contentDesc.isNotEmpty())
                identifier = this.contentDesc*/
            else if (this.resourceId != null && this.resourceId.isNotEmpty())
                identifier = this.resourceId
            else
                identifier = "" //this.toShortString()

            return "{${this.boundsString}\t${this.packageName}\t${this.className}\t$identifier}"
        }

        override fun toString(): String {
            val sb = StringBuilder()

            children.forEach{ p-> sb.append(p.toString())}

            val childrenVal = sb.toString()

            return "{${this.value.toFmtString()}$childrenVal}"
        }
    }

    internal fun addChildren(node : Node, widgets : List<Widget>) {
        val children = widgets
                .filter { p -> (p.parent == node.value) && (p.isVisibleOnCurrentDeviceDisplay)}

        children
                .forEach { p -> node.children.add(Node(p, ArrayList())) }

        node.children
                .forEach { p -> addChildren(p, widgets) }

    }

    internal fun getDissimilarity(s1: String, s2: String): Double{
        val parser = BracketStringInputParser()

        val s1Fmt = parser.fromString(s1)
        val s2Fmt = parser.fromString(s2)

        // Initialise APTED. All operations have cost 1
        val apted = APTED<CustomCostModel, StringNodeData>(CustomCostModel())

        return apted.computeEditDistance(s1Fmt, s2Fmt).toDouble()
    }

    internal fun loadTree(widgets : List<Widget>): List<Node>{
        val rootWidget = widgets
                .asSequence()
                .filter { p -> p.parent == null}
                .toList()
        val treeRoot : MutableList<Node> = ArrayList()

        rootWidget.forEach { p -> treeRoot.add(Node(p, ArrayList())) }

        treeRoot.forEach { p -> this.addChildren(p, widgets) }
        return treeRoot
    }

    internal fun loadTree(data : String): List<Node>{
        val dump = UiautomatorWindowDump(data, Dimension(1080, 1794), "com.netflix.mediaclient")
        val widgets = dump.guiState.widgets

        return this.loadTree(widgets)
    }

    internal fun treeSize(root: Node): Int{
        return (if (root.value.isVisibleOnCurrentDeviceDisplay) 1 else 0) + root.children.map { p-> this.treeSize((p)) }.sum()
    }

    @Test
    fun calcDissimilarityFile(){
        assert(true)
        /*val ui1File = Paths.get("/Users/nataniel/Downloads/dump1.xml")
        val ui2File = Paths.get("/Users/nataniel/Downloads/dump2.xml")
        try {
            val ui1 = String(Files.readAllBytes(ui1File))
            val ui2 = String(Files.readAllBytes(ui2File))

            val s1 = this.loadTree(ui1)
            val s2 = this.loadTree(ui2)

            System.out.println("-----")
            System.out.println(s1)
            System.out.println(s2)
            System.out.println("-----")
            val sim = this.getDissimilarity(s1.toString(), s2.toString())
            System.out.println(sim)

            val s1Size = s1.map { p -> this.treeSize(p) }.sum()
            val s2Size = s2.map { p -> this.treeSize(p) }.sum()
            System.out.println(s1Size)
            System.out.println(s2Size)

        }
        catch (e : IOException){
            Assert.fail()
        }*/
    }

    internal fun loadActions(explorationResult: ExplorationResult): List<Point> {
        val points: MutableList<Point> = ArrayList()

        val explRes = explorationResult.internalResult.first()
        explRes.actRess
                .forEachIndexed { index, action ->
                    if (action.result.guiSnapshot.guiState.belongsToApp(explRes.packageName)) {
                        val ui = action.result.guiSnapshot.guiState.widgets
                        points.add(Point(index, ui))
                    }
                }

        return points
    }

    internal fun cluster(points: List<Point>, THRESHOLD: Double, tmpFile: Path): List<Cluster>{
        for (i in 5..points.size) {

            val kMeans = KMeans()
            kMeans.init(points, i, THRESHOLD)
            val clusters = kMeans.calculate(tmpFile)

            val maxError = kMeans.maxError()

            if (maxError < THRESHOLD) {
                // Serialize
                if (!Files.exists(tmpFile))
                    kMeans.serialize(tmpFile)

                System.out.println("============")
                System.out.println("Best result:")
                kMeans.plotClusters()
                return clusters
            }
        }

        System.out.println("============")
        System.out.println("No clusters found")

        return ArrayList()
    }

    internal fun clusterWidgets(mappedData: HashMap<Int, MutableList<Point>>): HashMap<Int, MutableList<Widget>>{
        val clusteredWidgets = HashMap<Int, MutableList<Widget>>()

        mappedData.forEach { i, points ->
            clusteredWidgets[i] = ArrayList()

            points.forEachIndexed { idx, p ->

                p.getVisibleWidgets().forEach { w ->
                    if (!clusteredWidgets[i]!!.contains(w))
                        clusteredWidgets[i]!!.add(w)
                }
            }
        }

        clusteredWidgets.forEach { t, u ->
            println("Widgets cluster: $t\t Size: ${u.size}\t $u")
        }

        return clusteredWidgets
    }

    internal fun clusterWidgets(clusteredScreens: List<Cluster>): HashMap<Int, MutableList<Widget>>{
        val clusteredWidgets = HashMap<Int, MutableList<Widget>>()

        clusteredScreens.forEach { c ->
            clusteredWidgets[c.id] = ArrayList()

            c.getPoints().forEachIndexed { idx, p ->

                p.getVisibleWidgets().forEach { w ->
                    if (!clusteredWidgets[c.id]!!.contains(w))
                        clusteredWidgets[c.id]!!.add(w)
                }
            }
        }

        clusteredWidgets.forEach { t, u ->
            println("Widgets cluster: $t\t Size: ${u.size}\t $u")
        }

        return clusteredWidgets
    }

    internal fun mapScreensToClusters(packageName: String,
                                      actions: List<RunnableExplorationActionWithResult>,
                                      clusters : List<Cluster>,
                                      THRESHOLD: Double,
                                      tmpFile: Path): HashMap<Int, MutableList<Point>>{
        println("Mapping screens to the initial exploration")
        val map : HashMap<Int, MutableList<Point>> = HashMap()
        val remaining : MutableList<Point> = ArrayList()

        clusters.forEach { c -> map[c.id] = ArrayList() }

        actions.forEachIndexed { idx, a ->
            try {
                if (a.result.guiSnapshot.guiState.belongsToApp(packageName)) {
                    val point = Point(idx, a.result.guiSnapshot.guiState.widgets)

                    val distanceArray = clusters
                            .sortedBy { p -> p.id }
                            .asSequence()
                            .map { c -> Point.distance(point, c.centroid!!) }
                    val minDist = distanceArray.min() ?: Double.MAX_VALUE

                    if (minDist < THRESHOLD) {
                        val bestCluster = clusters[distanceArray.indexOf(minDist)]

                        map[bestCluster.id]!!.add(point)
                    } else
                        remaining.add(point)
                }
            }
            catch (e: ForbiddenOperationError){
                println("ERROR: " + e.message)
            }
        }

        println("Mapped ${actions.size - remaining.size}/${actions.size} screens")

        /*println("Clustering new screens")
        val newScreenClusters = cluster(remaining, THRESHOLD, tmpFile)

        newScreenClusters.forEach { nC ->
            map[nC.id * -1] = ArrayList()
            map[nC.id * -1]!!.addAll(nC.getPoints())
        }*/

        println("Mapped screens")
        map.forEach { t, u ->
            val status : String

            if (t < 0)
                status = "+"
            else if (u.isEmpty())
                status = "-"
            else
                status = " "

            println("$status Screen $t\t Size: ${u.size}\t")// $u")
        }

        return map
    }

    //@Test
    fun clusterScreens() {
        //val initialExplDir = Paths.get("data", "codeadore.textgram_3.0.10", "0_5215842561409541734")
        //val baseDir = Paths.get("C:\\Users\\natan_000\\Downloads\\RemovePermissions\\data_full")
        val baseDir = Paths.get("data")
        val THRESHOLD = 30.0

        Files.list(baseDir).forEach { appDir ->

            val appResults : MutableList<Result> = ArrayList()

            val initialExplDirCandidates = Files.list(appDir).filter { p -> p.fileName.toString().startsWith("0_") }.findFirst()

            if (initialExplDirCandidates.isPresent) {
                val initialExplDir = initialExplDirCandidates.get()

                Files.list(initialExplDir.parent)
                        .filter { p -> p.fileName != initialExplDir.fileName }
                        .forEach { p ->
                            if (p.isDirectory) {
                                val scenarioDir = p
                                val resPair = this.clusterScreens(initialExplDir, scenarioDir, THRESHOLD)

                                val objRes = resPair.first
                                appResults.add(objRes)

                                val res = resPair.second

                                val report = scenarioDir.resolve("experiment_report_$THRESHOLD.txt")
                                try {
                                    Files.write(report, res.toByteArray())
                                } catch(e: IOException) {
                                    println(e.message)
                                }
                            }
                        }

                this.saveAppResults(appDir, THRESHOLD, appResults)
            }
        }
    }

    internal fun saveAppResults(appDir : Path, THRESHOLD: Double, appResults : List<Result>){
        val sb = StringBuilder()

        appResults.forEach { p -> sb.append(p.toString() + "\r\n") }

        val reportFile = appDir.resolve("complete_report_$THRESHOLD.txt")
        try {
            Files.write(reportFile, sb.toString().toByteArray())
        } catch(e: IOException) {
            println(e.message)
        }
    }

    internal fun clusterScreens(initialExplDir: Path, scenarioDir: Path, THRESHOLD: Double): Pair<Result, String>{
        val initialExpl = ExplorationResult(initialExplDir, report = true)

        val points = this.loadActions(initialExpl)

        val clusteredScreens = this.cluster(points, THRESHOLD, initialExplDir.resolve("clusteredData_$THRESHOLD.dat"))
        val clusteredWidgets = this.clusterWidgets(clusteredScreens)

        val scenario = ExplorationResult(scenarioDir, report = true)

        val scenarioMapping = this.mapScreensToClusters(scenario.internalResult.first().packageName,
                scenario.internalResult.first().actRess, clusteredScreens, THRESHOLD,
                scenarioDir.resolve("clusteredData_$THRESHOLD.dat"))

        val finalMapping = this.clusterWidgets(scenarioMapping)

        val sbRes = StringBuilder()
        sbRes.append("===========\r\n")
        sbRes.append("Final result\r\n")

        var totalLoss = 0.0
        var totalSize = 0.0

        val screens : MutableList<Screen> = ArrayList()

        finalMapping.forEach { idx, widgets ->
            val nrOrigWidgets : Int
            val widgetsOnlyInitial : List<Widget>
            val widgetsOnlyNew : List<Widget>

            if (clusteredWidgets.containsKey(idx)) {
                nrOrigWidgets = clusteredWidgets[idx]!!.size
                widgetsOnlyInitial = clusteredWidgets[idx]!!
                  .filterNot { w -> widgets.contains(w) }
                widgetsOnlyNew = widgets
                  .filterNot { w -> clusteredWidgets[idx]!!.contains(w) }
            }
            else {
                nrOrigWidgets = 0
                widgetsOnlyInitial = ArrayList()
                widgetsOnlyNew = widgets
            }

            val status : String

            if (idx < 0)
                status = "+"
            else if (widgets.isEmpty())
                status = "-"
            else
                status = " "

            sbRes.append("$status Screen $idx\t IEX size: $nrOrigWidgets\t SCN size: ${widgets.size}\t IEX only: ${widgetsOnlyInitial.size}\t SCN only: ${widgetsOnlyNew.size}\t Loss: ${widgetsOnlyInitial.size.toDouble() / maxOf(nrOrigWidgets, 1).toDouble()}\r\n")

            screens.add(Screen(status, idx, nrOrigWidgets, widgets.size, widgetsOnlyInitial.size, widgetsOnlyNew.size,
              widgetsOnlyInitial.size.toDouble() / maxOf(nrOrigWidgets, 1).toDouble()))

            totalLoss += widgetsOnlyInitial.size.toDouble()
            totalSize += nrOrigWidgets.toDouble()
        }

        sbRes.append("* TOTAL LOSS: ${totalLoss / totalSize}\r\n")

        val res = Result(scenarioDir, screens)
        return Pair(res, sbRes.toString())
    }
}