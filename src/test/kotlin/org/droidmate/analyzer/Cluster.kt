package org.droidmate.analyzer

import java.io.Serializable

class Cluster internal constructor(var id: Int, val DEBUG: Boolean = false): Serializable {

    private val points: MutableList<Point> = ArrayList<Point>()
    var centroid: Point? = null

    fun getPoints(): List<Point> {
        return points
    }

    fun addPoint(point: Point) {
        points.add(point)
    }

    fun clear() {
        points.clear()
    }

    fun maxError(): Double{
        val maxVals = points
                .map { p -> Point.distance(p, centroid?:p) }
                .max()
        val maxDist = maxVals?: -1.0

        return maxDist
    }

    fun isNotEmpty(): Boolean{
        return this.points.isNotEmpty()
    }

    fun plotCluster(otherClusters: List<Cluster>) {

        if (points.isNotEmpty()) {

            val maxDist = this.maxError()

            println("\n")
            println("[Cluster: $id]")
            println("[Size: ${points.size}]")
            println("[Max error: " + (if (maxDist >= 0) maxDist else "Empty") + "]")

            val sb = StringBuilder()
            otherClusters.forEach { c ->
                if ((c != this) && (c.isNotEmpty())){
                    sb.append("(${c.id}, ${Point.distance(this.centroid!!, c.centroid!!)})\t")
                }
            }

            println("[Distance: $sb]")

            if (DEBUG) {
                println("[Centroid: $centroid]")
                println("[Points: \n")
                println("]")
            }
        }
        else if (DEBUG){
            println("\n")
            println("[Cluster: $id is Empty]")
        }
    }

}