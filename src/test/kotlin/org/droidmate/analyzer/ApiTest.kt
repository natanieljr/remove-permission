package org.droidmate.analyzer

import org.droidmate.analyzer.api.Api
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters

/**
 * Unit tests for Api class
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class ApiTest {
    @Test
    fun BuildApiTest() {
        val api = Api.build("my.Class", "aMethod", "", "")

        assertTrue(api.toString() == "my.Class->aMethod()\t")
        assertTrue(api.uri.isEmpty())
        assertTrue(api.uriParamName.isEmpty())
        assertFalse(api.hasRestriction())

        val apiWithUri = Api.build("my.Class", "aMethod", "(android.net.Uri)", "content://test")
        assertTrue(apiWithUri.toString() == "my.Class->aMethod(android.net.Uri)\tcontent://test")
        assertTrue(apiWithUri.uri == "content://test")
        assertTrue(apiWithUri.uriParamName == "p0")
        assertFalse(apiWithUri.hasRestriction())

        val apiWithUriNoBraces = Api.build("my.Class", "aMethod", "android.net.Uri", "content://test")
        assertTrue(apiWithUriNoBraces.toString() == "my.Class->aMethod(android.net.Uri)\tcontent://test")
        assertTrue(apiWithUriNoBraces.uri == "content://test")
        assertTrue(apiWithUriNoBraces.uriParamName == "p0")
        assertFalse(apiWithUriNoBraces.hasRestriction())

        assertFalse(api == apiWithUri)
        assertTrue(apiWithUri == apiWithUriNoBraces)
    }

    @Test
    fun GetMethodNameFromMethodSignatureTest() {
        val signature1 = "java.lang.Runtime->load(java.lang.String)"
        val methodName1 = Api.getMethodNameFromSignature(signature1)
        assertTrue(methodName1 == "java.lang.Runtime->load")

        val signature2 = "android.accounts.AccountManager->getAuthTokenLabel(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val methodName2 = Api.getMethodNameFromSignature(signature2)
        assertTrue(methodName2 == "android.accounts.AccountManager->getAuthTokenLabel")

        val ctor1 = "java.lang.Runtime-><init>(java.lang.String)"
        val paramsCtor1 = Api.getMethodNameFromSignature(ctor1)
        assertTrue(paramsCtor1 == "java.lang.Runtime-><init>")

        val ctor2 = "android.accounts.AccountManager-><init>(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val paramsCtor2 = Api.getMethodNameFromSignature(ctor2)
        assertTrue(paramsCtor2 == "android.accounts.AccountManager-><init>")
    }

    @Test
    fun GetParamsFromMethodSignatureTest() {
        val signature1 = "java.lang.Runtime->load(java.lang.String)"
        val params1 = Api.getParamsFromMethodSignature(signature1)
        assertTrue(params1 == "java.lang.String")

        val signature2 = "android.accounts.AccountManager->getAuthTokenLabel(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val params2 = Api.getParamsFromMethodSignature(signature2)
        assertTrue(params2 == "java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler")

        val ctor1 = "java.lang.Runtime-><init>(java.lang.String)"
        val paramsCtor1 = Api.getParamsFromMethodSignature(ctor1)
        assertTrue(paramsCtor1 == "java.lang.String")

        val ctor2 = "android.accounts.AccountManager-><init>(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val paramsCtor2 = Api.getParamsFromMethodSignature(ctor2)
        assertTrue(paramsCtor2 == "java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler")
    }

}

/*

package org.droidmate.analyzer

import org.droidmate.device.datatypes.Widget
import java.util.Random

class KMeans internal constructor() {

    //Number of Clusters. This metric should be related to the number of points
    private val MAX_CLUSTERS = 3

    private var points: List<Point>? = null
    private val clusters: MutableList<Cluster>

    init {
        this.points = ArrayList<Point>()
        this.clusters = ArrayList<Cluster>()
    }

    //Initializes the process
    fun init(points: List<Point>) {
        //Create Points
        this.points = points

        val random = Random()

        //Create Clusters
        //Set Random Centroids
        var done = false
        for (i in 0..MAX_CLUSTERS - 1) {
            val cluster = Cluster(i)
            val centroid = this.points!![random.nextInt(MAX_CLUSTERS)]

            if (clusters.any{ p -> p.centroid == centroid }) {
                if (done)
                    break
                done = false
            }
            else {
                cluster.centroid = centroid
                clusters.add(cluster)
            }
        }

        //Print Initial state
        plotClusters()
    }

    fun plotClusters() {
        clusters.forEach { it.plotCluster() }
    }

    //The process to calculate the K Means, with iterating method.
    fun calculate() {
        var finish = false
        var iteration = 0

        // Add in new data, one at a time, recalculating centroids with each new one.
        while (!finish) {
            //Clear cluster state
            clearClusters()

            val lastCentroids = centroids

            //Assign points to the closer cluster
            assignCluster()

            //Calculate new centroids.
            calculateCentroids()

            iteration++

            val currentCentroids = centroids

            //Calculates total distance between new and old Centroids
            val distance = lastCentroids.indices.sumByDouble { Point.distance(lastCentroids[it], currentCentroids[it]) }
            println("#################")
            println("Iteration: " + iteration)
            println("Centroid distances: " + distance)
            plotClusters()

            if (distance == 0.0) {
                finish = true
            }
        }
    }

    private fun clearClusters() {
        for (cluster in clusters) {
            cluster.clear()
        }
    }

    private val centroids: List<Point>
        get() {
            val centroids = ArrayList<Point>(clusters.size)
            clusters
                    .map { it.centroid }
                    .mapTo(centroids) { Point(it.data) }
            return centroids
        }

    private fun assignCluster() {
        val max = java.lang.Double.MAX_VALUE
        var min : Double
        var cluster = 0
        var distance : Double

        for (point in points!!) {
            min = max
            for (i in 0..clusters.size - 1) {
                val c = clusters[i]
                distance = Point.distance(point, c.centroid)
                if (distance < min) {
                    min = distance
                    cluster = i
                }
            }
            point.cluster = cluster
            clusters[cluster].addPoint(point)
        }
    }

    private fun calculateCentroids() {
        for (cluster in clusters) {
            val common : MutableList<Widget> = ArrayList()
            val pointList = cluster.getPoints()
            val n_points = pointList.size

            val base = if (pointList.isNotEmpty()) pointList.first() else Point(ArrayList())
            val remainingPoints = pointList
                    .filter { p -> p != base }
                    .toList()

            base.data.forEach { p ->
                if (remainingPoints.all { x -> x.data.contains(p) })
                    common.add(p)
            }

            val centroid = cluster.centroid
            if(n_points > 0) {
                centroid.data = common
            }
        }
    }
}


 */


/*

package org.droidmate.analyzer

import java.util.ArrayList

class Cluster//Creates a new Cluster
internal constructor(var id: Int) {

    private val points: MutableList<Point> = ArrayList()
    var centroid: Point = Point(ArrayList())
        get
        set

    fun getPoints(): List<Point> {
        return points
    }

    fun addPoint(point: Point) {
        points.add(point)
    }

    fun clear() {
        points.clear()
    }

    fun plotCluster() {
        val maxDist = points.map{ p -> Point.distance(p, centroid) }.max() ?: -1

        println("[Cluster: $id]")
        println("[Centroid: $centroid]")
        println("[Max dist: $maxDist]")
        println("[Points: \n")
        for (p in points) {
            println(p)
        }
        println("]")
    }
}

/*

package org.droidmate.analyzer

import at.unisalzburg.apted.distance.APTED
import at.unisalzburg.apted.node.Node
import at.unisalzburg.apted.node.StringNodeData
import at.unisalzburg.apted.parser.BracketStringInputParser


import org.droidmate.analyzer.evaluation.CustomCostModel
import org.droidmate.device.datatypes.Widget

import java.util.ArrayList

class Point internal constructor(data: List<Widget>) {

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

    var data: List<Widget> = ArrayList()
    var cluster = 0

    init {
        this.data = data
    }

    override fun toString(): String {
        return data.toString();
    }

    companion object {

        internal fun addChildren(node : Node, widgets : List<Widget>) {
            val children = widgets
                    .filter { p -> (p.parent == node.value) && (p.isVisibleOnCurrentDeviceDisplay)}

            children
                    .forEach { p -> node.children.add(Node(p, ArrayList())) }

            node.children
                    .forEach { p -> addChildren(p, widgets) }

        }

        internal fun loadTree(widgets : List<Widget>): List<Node>{
            val rootWidget = widgets
                    .asSequence()
                    .filter { p -> p.parent == null}
                    .toList()
            val treeRoot : MutableList<Node> = ArrayList()

            rootWidget.forEach { p -> treeRoot.add(Node(p, ArrayList())) }

            treeRoot.forEach { p -> Point.addChildren(p, widgets) }
            return treeRoot
        }


        //Calculates the distance between two points.
        fun distance(p: Point, centroid: Point): Double {
            val firstVal = this.loadTree(p.data)
            val secondVal = this.loadTree(centroid.data)

            val parser = BracketStringInputParser()

            val firstFmt = parser.fromString(if (firstVal.isNotEmpty()) firstVal.toString() else "{}")
            val secondFmt = parser.fromString(if (secondVal.isNotEmpty()) secondVal.toString() else "{}")

            // Initialise APTED. All operations have cost 1
            val apted = APTED<CustomCostModel, StringNodeData>(CustomCostModel())

            return apted.computeEditDistance(firstFmt, secondFmt).toDouble()
        }
    }
}

 */

 */