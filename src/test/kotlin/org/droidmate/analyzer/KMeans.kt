/* 
 * KMeans.java ; Cluster.java ; Point.java
 *
 * Solution adapted from DataOnFocus
 *
*/
package org.droidmate.analyzer

import org.droidmate.device.datatypes.Widget
import java.io.Serializable
import java.nio.file.Files

import java.util.ArrayList
import java.util.Random
import sun.plugin2.liveconnect.ArgumentHelper.readObject
import java.io.ObjectInputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import sun.plugin2.liveconnect.ArgumentHelper.writeObject
import java.io.ObjectOutputStream
import java.io.FileOutputStream




class KMeans constructor(val DEBUG : Boolean = false): Serializable{

    private val MAX_ITERATIONS = 50
    private var THRESHOLD : Double = 0.0

    private var points: List<Point> = ArrayList()
    private val clusters: MutableList<Cluster> = ArrayList()

    //Initializes the process
    fun init(points: List<Point>, numClusters : Int, THRESHOLD: Double) {
        this.THRESHOLD = THRESHOLD
        this.points = points

        val seed = System.currentTimeMillis()
        //if (DEBUG)
        //    println("Using following initialization seed: $seed")
        val random = Random(seed)

        //Create Clusters
        //Set Random Centroids
        var attempt = 0
        val internalCentroidList : MutableList<Point> = ArrayList()

        while (clusters.size < numClusters) {
            val centroid = this.points[random.nextInt(points.size)]

            val distArray = internalCentroidList.map { p -> Point.distance(p, centroid) }.min() ?: Double.MAX_VALUE

            if ((distArray > this.THRESHOLD * 5) || (attempt++ > 50)) {
                val cluster = Cluster(clusters.size, DEBUG)
                cluster.centroid = centroid
                clusters.add(cluster)

                internalCentroidList.add(centroid)
                attempt = 0
            }
        }

        //Print Initial state
        if (DEBUG)
            plotClusters()
    }

    fun plotClusters() {
        clusters
                .filter{ p -> p.isNotEmpty() }
                .forEach { c -> c.plotCluster(this.clusters) }
    }

    internal fun loadFromFile(tmpFile: Path): KMeans{
        // Try to reuse file (if exists)
        if (Files.exists(tmpFile)){
            try{
                val fileIn = FileInputStream(tmpFile.toFile())
                val input = ObjectInputStream(fileIn)
                val serialized = input.readObject() as KMeans
                input.close()
                fileIn.close()

                return serialized
            }
            catch(e : IOException){
                Files.delete(tmpFile)
                println(e.message)
            }
        }

        return KMeans()
    }

    //The process to calculate the K Means, with iterating method.
    fun calculate(tmpFile : Path): List<Cluster> {
        val serializedData = this.loadFromFile(tmpFile)
        if (serializedData.clusters.isNotEmpty()) {
            this.clusters.clear()
            this.clusters.addAll(serializedData.clusters
                    .filter { c -> c.isNotEmpty() }
                    .toList())

            return this.clusters
        }

        var finish = false
        var iteration = 0

        // Add in new data, one at a time, recalculating centroids with each new one.
        while (!finish) {
            val lastClustersSize = clusters.map { c -> c.getPoints().size }

            //Clear cluster state
            clearClusters()

            val lastCentroids = centroids

            //Assign points to the closer cluster
            assignCluster()

            //Calculate new centroids.
            calculateCentroids(lastClustersSize)

            iteration++

            val currentCentroids = centroids

            //Calculates total distance between new and old Centroids
            val distance = lastCentroids.indices.sumByDouble { Point.distance(lastCentroids[it], currentCentroids[it]) }

            if (DEBUG) {
                println("#################")
                println("Iteration: " + iteration)
                println("Centroid variation: " + distance)
                plotClusters()
            }

            assert(clusters.map { c -> c.getPoints().size }.sum() == points.size)

            if ((distance == 0.0) || (iteration > MAX_ITERATIONS)){
                finish = true
            }
        }

        println("Nr clusters: ${clusters.size}\tMax error: ${this.maxError()}")

        // Necessary due to bad initialization of points
        this.mergeEquivalentClusters()

        return clusters
                .filter{ p -> p.isNotEmpty() }
                .toList()
    }

    fun mergeEquivalentClusters(){
        val mergedClusters : MutableList<Cluster> = ArrayList()

        clusters.forEach { c ->
            if (c.isNotEmpty()) {
                val distArray = mergedClusters.map { p -> Point.distance(p.centroid!!, c.centroid!!) }
                val minDist = distArray.min() ?: Double.MAX_VALUE

                if (minDist < this.THRESHOLD) {
                    val idx = distArray.indexOf(minDist)
                    val newCluster = mergedClusters[idx]

                    c.getPoints().forEach { p ->
                        newCluster.addPoint(p)
                        p.cluster = newCluster.id
                    }
                } else {
                    mergedClusters.add(c)
                }
            }
        }

        this.clusters.clear()
        this.clusters.addAll(mergedClusters)
    }

    fun serialize(tmpFile: Path){
        // Store temporary results
        try{
            Files.deleteIfExists(tmpFile)
            val fileOut = FileOutputStream(tmpFile.toFile())
            val out = ObjectOutputStream(fileOut)
            out.writeObject(this)
            out.close()
            fileOut.close()
        }
        catch(e : IOException){
            println(e.message)
        }
    }

    private fun clearClusters() {
        for (cluster in clusters) {
            cluster.clear()
        }
    }

    private val centroids: List<Point>
        get() {
            val centroids : MutableList<Point> = ArrayList()

            clusters.forEach{ cluster ->
                val aux = cluster.centroid!!
                val point = Point(-1, aux.widgets)
                centroids.add(point)
            }

            return centroids
        }

    private fun assignCluster() {
        for (point in points) {

            val minDistArray = clusters
                    .map { c -> Point.distance(point, c.centroid ?: point)}

            val minDist = minDistArray
                    .min() ?: Double.MAX_VALUE

            val cluster = clusters[minDistArray.indexOf(minDist)]
            point.cluster = cluster.id
            clusters[cluster.id].addPoint(point)
        }
    }

    private fun calculateCentroids(lastClusters: List<Int>) {
        clusters.forEachIndexed { idx, cluster ->
            val origClusterSize = lastClusters[idx]

            if (origClusterSize != cluster.getPoints().size) {

                val common = ArrayList<Widget>()
                val list = cluster.getPoints()
                val n_points = list.size

                if (n_points > 0) {
                    val base = list[0].widgets

                    base.forEach { baseWidget ->
                        if (list
                                .all { point -> point.widgets.contains(baseWidget) })
                            common.add(baseWidget)
                    }

                    // If there's no common widget, use "best" element //random element
                    if (common.isEmpty()) {
                        val sampleIdx : MutableList<Int> = ArrayList()
                        val sampleList : MutableList<Point> = ArrayList()

                        for(i in 0..minOf(19, list.size)){
                            val item = Random().nextInt(list.size)
                            sampleList.add(list[item])
                            sampleIdx.add(item)
                        }

                        //val cartesianProduct = list.map { p -> list.map { q -> Point.distance(p, q) }.sum() }
                        val cartesianProduct = sampleList.map { p -> sampleList.map { q -> Point.distance(p, q) }.sum() }

                        val bestVal = cartesianProduct.min()!!
                        val bestItem = list[sampleIdx[cartesianProduct.indexOf(bestVal)]]

                        common.addAll(bestItem.widgets)
                        //common.addAll(list[Random().nextInt(list.size)].widgets)
                    }

                    val centroid = cluster.centroid!!
                    centroid.widgets = common
                }
            }
        }
    }

    fun maxError(): Double{
        return clusters
                .map { p -> p.maxError() }
                .max()!!
    }
}