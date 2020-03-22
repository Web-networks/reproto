package raid.neuroide.reproto.crdt

import kotlinx.serialization.Serializable
import kotlin.math.max


@Serializable
data class LamportTimestamp(val time: Long, val site: String) : Comparable<LamportTimestamp> {
    override fun compareTo(other: LamportTimestamp) =
        compareValuesBy(this, other, LamportTimestamp::time, LamportTimestamp::site)
}

@Serializable
class LamportClock(private val site: String) {
    private var time: Long = 0

    val value
        get() = LamportTimestamp(time, site)

    fun next() = LamportTimestamp(++time, site)

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
