package org.droidmate.analyzer

/*import weka.clusterers.SimpleKMeans
import weka.core.**/

/**
 * Created by nataniel on 17.07.17.
 */
/*class SimilarityBasedKMeans : SimpleKMeans() {

    public inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int)->INNER): Array<Array<INNER>>
            = Array(sizeOuter) { Array<INNER>(sizeInner, innerInit) }
    public fun array2dOfInt(sizeOuter: Int, sizeInner: Int): Array<IntArray>
            = Array(sizeOuter) { IntArray(sizeInner) }
    public fun array2dOfLong(sizeOuter: Int, sizeInner: Int): Array<LongArray>
            = Array(sizeOuter) { LongArray(sizeInner) }
    public fun array2dOfByte(sizeOuter: Int, sizeInner: Int): Array<ByteArray>
            = Array(sizeOuter) { ByteArray(sizeInner) }
    public fun array2dOfChar(sizeOuter: Int, sizeInner: Int): Array<CharArray>
            = Array(sizeOuter) { CharArray(sizeInner) }
    public fun array2dOfBoolean(sizeOuter: Int, sizeInner: Int): Array<BooleanArray>
            = Array(sizeOuter) { BooleanArray(sizeInner) }
    public fun array2dOfDouble(sizeOuter: Int, sizeInner: Int): Array<DoubleArray>
            = Array(sizeOuter) { DoubleArray(sizeInner) }

    init {
        this.m_DistanceFunction = SimilarityBasedDistance()
    }

    @Override
    override fun getCapabilities(): Capabilities {
        val result = super.getCapabilities()
        result.disableAll()
        result.enable(Capabilities.Capability.NO_CLASS)
        result.enable(Capabilities.Capability.STRING_ATTRIBUTES)
        return result
    }

    @Override
    override fun moveCentroid(centroidIndex: Int, members: Instances,
                               updateClusterInfo: Boolean, addToCentroidInstances: Boolean): DoubleArray {

        val vals = DoubleArray(members.numAttributes())
        val nominalDists = array2dOfDouble(members.numInstances(), members.numAttributes())
        val weightMissing = DoubleArray(members.numAttributes())
        val weightNonMissing = DoubleArray(members.numAttributes())

        // Quickly calculate some relevant statistics
        for (j in 0..members.numAttributes() - 1) {
            if (members.attribute(j).isNominal) {
                nominalDists[j] = DoubleArray(members.attribute(j).numValues())
            }
        }
        for (inst in members) {
            for (j in 0..members.numAttributes() - 1) {
                if (inst.isMissing(j)) {
                    weightMissing[j] += inst.weight()
                } else {
                    weightNonMissing[j] += inst.weight()
                    if (members.attribute(j).isNumeric) {
                        vals[j] += inst.weight() * inst.value(j) // Will be overwritten in Manhattan case
                    } else {
                        nominalDists[j][inst.value(j).toInt()] += inst.weight()
                    }
                }
            }
        }
        for (j in 0..members.numAttributes() - 1) {
            if (members.attribute(j).isNumeric) {
                if (weightNonMissing[j] > 0) {
                    vals[j] /= weightNonMissing[j]
                } else {
                    vals[j] = Utils.missingValue()
                }
            } else {
                var max = -java.lang.Double.MAX_VALUE
                var maxIndex = -1.0
                for (i in 0..nominalDists[j].size - 1) {
                    if (nominalDists[j][i] > max) {
                        max = nominalDists[j][i]
                        maxIndex = i.toDouble()
                    }
                    if (max < weightMissing[j]) {
                        vals[j] = Utils.missingValue()
                    } else {
                        vals[j] = maxIndex
                    }
                }
            }
        }

        if (m_DistanceFunction is ManhattanDistance) {

            // Need to replace means by medians
            var sortedMembers: Instances? = null
            val middle = (members.numInstances() - 1) / 2
            val dataIsEven = members.numInstances() % 2 == 0
            if (m_PreserveOrder) {
                sortedMembers = members
            } else {
                sortedMembers = Instances(members)
            }
            for (j in 0..members.numAttributes() - 1) {
                if (weightNonMissing[j] > 0 && members.attribute(j).isNumeric) {
                    // singleton special case
                    if (members.numInstances() == 1) {
                        vals[j] = members.instance(0).value(j)
                    } else {
                        vals[j] = sortedMembers.kthSmallestValue(j, middle + 1)
                        if (dataIsEven) {
                            vals[j] = (vals[j] + sortedMembers.kthSmallestValue(j, middle + 2)) / 2
                        }
                    }
                }
            }
        }

        if (updateClusterInfo) {
            for (j in 0..members.numAttributes() - 1) {
                m_ClusterMissingCounts[centroidIndex][j] = weightMissing[j]
                m_ClusterNominalCounts[centroidIndex][j] = nominalDists[j]
            }
        }

        if (addToCentroidInstances) {
            m_ClusterCentroids.add(DenseInstance(1.0, vals))
        }

        return vals
    }
}
*/