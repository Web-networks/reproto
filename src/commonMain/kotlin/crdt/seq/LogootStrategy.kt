package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


private fun List<Doublet>.indexAt(i: Int) =
    if (i < size) {
        this[i].index
    } else {
        0
    }


@Serializable
object LogootStrategy : AllocationStrategy {
    // Copypasted from https://github.com/PascalUrso/ReplicationBenchmark/blob/7cb464b8e07f7dc3931a9586d390cf1cdaaf4bfa/src/jbenchmarker/logoot/BoundaryStrategy.java#L53
    // with minor changes
    override fun allocatePosition(
        left: List<Doublet>,
        right: List<Doublet>,
        site: String
    ): List<Doublet> {
        val commonLen: Int = min(left.size, right.size)
        var level = 0
        while (level < commonLen && left[level] == right[level]) {
            level++
        }

        if (commonLen == left.size && commonLen == right.size && commonLen == level ||
            commonLen == level && left.size > right.size ||
            level < commonLen && left[level] > right[level]
        ) throw IllegalArgumentException("left must be less than right")

        var space = right.indexAt(level) - left.indexAt(level) - 1
        if (space < 1) {
            space = max(0, space)
            while (space == 0) {
                level++
                space = getLevelMax(level) - left.indexAt(level) + right.indexAt(level)
            }
        }

        val offset = Random.nextInt(min(space, getLevelBoundary(level))) + 1
        return materialize(left, right, level, offset, site)
    }

    private fun getLevelMax(level: Int): Int = Int.MAX_VALUE

    private fun getLevelBoundary(level: Int): Int = 20

    // Copypasted from https://github.com/PascalUrso/ReplicationBenchmark/blob/7cb464b8e07f7dc3931a9586d390cf1cdaaf4bfa/src/jbenchmarker/logoot/LogootIdentifier.java#L85
    // with minor changes
    // Attention: some suspicious code was rewritten
    private fun materialize(
        left: List<Doublet>,
        right: List<Doublet>,
        level: Int,
        offset: Int,
        site: String
    ): List<Doublet> {
        val digits = (
                left.map { it.index } +
                        generateSequence { 0 }.take(max(0, level - left.size + 1))
                ).toMutableList()
        var leftTakenIndex: Int = level

        // digits += offset
        if (getLevelMax(leftTakenIndex) - digits[level] >= offset) {
            digits[leftTakenIndex] += offset
        } else {
            digits[leftTakenIndex] += offset - getLevelMax(leftTakenIndex) - 1
            --leftTakenIndex
            while (digits[leftTakenIndex] == getLevelMax(leftTakenIndex)) {
                digits[leftTakenIndex] = 0
                --leftTakenIndex
            }
            digits[leftTakenIndex] = digits[leftTakenIndex] + 1
        }

        val result: MutableList<Doublet> = mutableListOf()
        var i = 0

        while (i < left.size && digits[i] == left[i].index) {
            result.add(Doublet(digits[i], left[i].site))
            i++
        }
        while (i < right.size && digits[i] == right[i].index) {
            result.add(Doublet(right[i].index, right[i].site))
            i++
        }
        while (i <= level) {
            result.add(Doublet(digits[i], site))
            i++
        }

        return result
    }
}
