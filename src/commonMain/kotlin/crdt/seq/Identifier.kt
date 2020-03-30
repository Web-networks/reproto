package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
data class Identifier(val position: List<Doublet>, val clock: Long) : Comparable<Identifier> {
    override fun compareTo(other: Identifier): Int {
        val maxlen = max(position.size, other.position.size)
        for (i in 0 until maxlen) {
            if (i >= position.size)
                return -1
            if (i >= other.position.size)
                return 1

            val cmp = position[i].compareTo(other.position[i])
            if (cmp != 0)
                return cmp
        }

        return compareValues(clock, other.clock)
    }
}
