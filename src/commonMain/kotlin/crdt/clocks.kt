package raid.neuroide.reproto.crdt

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import raid.neuroide.reproto.common.DelegatedMapSerializer
import kotlin.math.max


@Serializable
data class LamportTimestamp(val time: Long, val site: String) : Comparable<LamportTimestamp> {
    override fun compareTo(other: LamportTimestamp) =
        compareValuesBy(this, other, LamportTimestamp::time, LamportTimestamp::site)
}

@Serializable
class LamportClock(private val siteId: LocalSiteId) {
    private var time: Long = 0

    val value
        get() = LamportTimestamp(time, siteId.id)

    fun next() = LamportTimestamp(++time, siteId.id)

    fun update(timestamp: LamportTimestamp) {
        time = max(time, timestamp.time)
    }
}

@Serializable
class PlainClock {
    private var time: Long = 0

    val value
        get() = time

    fun next() = ++time
}

@Serializable(with = VectorTimestamp.Serializer::class)
data class VectorTimestamp(val times: Map<String, Int>) : Comparable<VectorTimestamp> {
    fun tryCompareTo(other: VectorTimestamp): Int? {
        val sites = times.keys + other.times.keys
        var result = 0
        for (site in sites) {
            val curResult = compareValues(times[site] ?: 0, other.times[site] ?: 0)
            if (result == 0) {
                result = curResult
            } else if (curResult != result) {
                return null
            }
        }
        return result
    }

    override fun compareTo(other: VectorTimestamp): Int {
        return tryCompareTo(other) ?: throw IncomparableException()
    }

    operator fun get(site: String) = times[site] ?: 0

    companion object {
        fun areComparable(a: VectorTimestamp, b: VectorTimestamp) = a.tryCompareTo(b) != null
    }

    class Serializer : DelegatedMapSerializer<VectorTimestamp, String, Int>(String.serializer(), Int.serializer()) {
        override fun extract(value: VectorTimestamp): Map<String, Int> {
            return value.times
        }

        override fun construct(map: Map<String, Int>): VectorTimestamp {
            return VectorTimestamp(map)
        }
    }
}

@Serializable
class VectorClock(private val siteId: LocalSiteId) {
    @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS") // to bypass the bug in kotlinx.serialization
    private val times: MutableMap<String, Int> = mutableMapOf((this?.siteId?.id ?: "") to 0)

    val value
        get() = VectorTimestamp(times)

    fun next(): VectorTimestamp {
        times[siteId.id] = (times[siteId.id] ?: 0) + 1
        return value
    }

    fun update(timestamp: VectorTimestamp) {
        for ((site, time) in timestamp.times) {
            times[site] = max(times[site] ?: 0, time)
        }
    }
}
