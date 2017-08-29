package org.droidmate.analyzer

import at.unisalzburg.apted.distance.APTED
import at.unisalzburg.apted.node.StringNodeData
import at.unisalzburg.apted.parser.BracketStringInputParser
import org.droidmate.analyzer.evaluation.CustomCostModel
/*import weka.core.*
import weka.core.neighboursearch.PerformanceStats*/

/**
 *
 * Implementing Similarity based comparison for widgets.<br></br>
 * @author Nataniel Borges Jr.
 * *
 * @version $Revision: 1 $
 */
/*class SimilarityBasedDistance : EuclideanDistance() {

    /**
     * Calculates the distance between two instances. Offers speed up (if the
     * distance function class in use supports it) in nearest neighbour search by
     * taking into account the cutOff or maximum distance. Depending on the
     * distance function class, post processing of the distances by
     * postProcessDistances(double []) may be required if this function is used.

     * @param first the first instance
     * *
     * @param second the second instance
     * *
     * @param cutOffValue If the distance being calculated becomes larger than
     * *          cutOffValue then the rest of the calculation is discarded.
     * *
     * @param stats the performance stats object
     * *
     * @return the distance between the two given instances or
     * *         Double.POSITIVE_INFINITY if the distance being calculated becomes
     * *         larger than cutOffValue.
     */
    override fun distance(first: Instance, second: Instance, cutOffValue: Double,
                          stats: PerformanceStats?): Double{
        val firstVal = first.stringValue(0)
        val secondVal = second.stringValue(0)

        val parser = BracketStringInputParser()

        val firstFmt = parser.fromString(firstVal)
        val secondFmt = parser.fromString(secondVal)

        // Initialise APTED. All operations have cost 1
        val apted = APTED<CustomCostModel, StringNodeData>(CustomCostModel())
        //StringUnitCostModel

        return apted.computeEditDistance(firstFmt, secondFmt).toDouble()
    }

    companion object {

        /** for serialization.  */
        private val serialVersionUID = 1058606223448907903L
    }
}
*/