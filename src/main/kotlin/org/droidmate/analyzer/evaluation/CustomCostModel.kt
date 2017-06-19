package org.droidmate.analyzer.evaluation

import at.unisalzburg.apted.costmodel.StringUnitCostModel
import at.unisalzburg.apted.node.Node
import at.unisalzburg.apted.node.StringNodeData

/**
 * Custom cost model where renaming one API costs only 0.5, otherwise, in a situation such as:
 * A{{B}{C}} <-> A{{C}{B}} would have cost 2, while A{{B}{C}} <-> A{{B}} would have cost 1
 */
class CustomCostModel : StringUnitCostModel() {
    /**
     * Calculates the cost of renaming the label of the source node to the label
     * of the destination node.

     * @param n1 a source node for rename.
     * *
     * @param n2 a destination node for rename.
     * *
     * @return `1` if labels of renamed nodes are equal, and `0` otherwise.
     */
    override fun ren(n1: Node<StringNodeData>, n2: Node<StringNodeData>): Float {
        return if (n1.nodeData.label == n2.nodeData.label) 0.0f else 0.5f
    }
}
