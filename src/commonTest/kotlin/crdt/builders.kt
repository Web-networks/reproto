package raid.neuroide.reproto.crdt

import raid.neuroide.reproto.crdt.seq.LogootStrategy
import raid.neuroide.reproto.crdt.seq.Sequence

class CrdtTestBuilder(private val upstream: InternalBroadcast) {
    private var siteId = 0

    fun lwwRegister(payload: String = "") = LWWRegister(payload, LamportClock(siteId++.toString()))
        .apply {
            upstream.addCrdt(this)
        }

    fun logoot() = Sequence(siteId++.toString(), LogootStrategy())
        .apply {
            upstream.addCrdt(this)
        }

    fun disconnect() {
        upstream.connected = false
    }

    fun connect() {
        upstream.connected = true
    }

    val Sequence.string
        get() = content.joinToString("")
}

inline fun crdtTest(func: CrdtTestBuilder.() -> Unit) {
    CrdtTestBuilder(InternalBroadcast()).func()
}

inline fun crdtTest(upstream: InternalBroadcast, func: CrdtTestBuilder.() -> Unit) {
    CrdtTestBuilder(upstream).func()
}
