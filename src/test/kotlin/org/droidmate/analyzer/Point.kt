package org.droidmate.analyzer

import at.unisalzburg.apted.distance.APTED
import at.unisalzburg.apted.node.StringNodeData
import at.unisalzburg.apted.parser.BracketStringInputParser
import org.droidmate.analyzer.evaluation.CustomCostModel
import org.droidmate.device.datatypes.Widget
import java.io.Serializable

class Point(val id: Int, var widgets: List<Widget>): Serializable {

    var cluster = 0
        set

    fun getVisibleWidgets(): List<Widget>{
        return this.widgets
                .filter{ p -> p.isVisibleOnCurrentDeviceDisplay }
                .toList()
    }

    override fun toString(): String {
        return widgets.toString()
    }

    override fun equals(other: Any?): Boolean {
        if ((other == null) || (other !is Point))
            return false

        return (other.id == this.id) && (other.widgets == this.widgets)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + widgets.hashCode()
        result = 31 * result + cluster
        return result
    }

    companion object {
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
                    .forEach { p -> node.children.add(Node(p, java.util.ArrayList())) }

            node.children
                    .forEach { p -> addChildren(p, widgets) }

        }

        internal fun loadTree(widgets : List<Widget>): List<Node>{
            val rootWidget = widgets
                    .asSequence()
                    .filter { p -> p.parent == null}
                    .toList()
            val treeRoot : MutableList<Node> = java.util.ArrayList()

            rootWidget.forEach { p -> treeRoot.add(Node(p, java.util.ArrayList())) }

            treeRoot.forEach { p -> this.addChildren(p, widgets) }
            return treeRoot
        }

        //Calculates the distance between two points.
        fun distance(p: Point, centroid: Point): Double {
            val s1 = this.loadTree(p.widgets)
            val s2 = this.loadTree(centroid.widgets)

            val parser = BracketStringInputParser()

            val s1Fmt = parser.fromString(if (s1.isNotEmpty()) s1.toString() else "{}")
            val s2Fmt = parser.fromString(if (s2.isNotEmpty()) s2.toString() else "{}")

            // Initialise APTED. All operations have cost 1
            val apted = APTED<CustomCostModel, StringNodeData>(CustomCostModel())

            return apted.computeEditDistance(s1Fmt, s2Fmt).toDouble()
        }
    }
}