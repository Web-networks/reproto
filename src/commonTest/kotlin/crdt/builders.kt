package raid.neuroide.reproto.crdt

import raid.neuroide.reproto.crdt.seq.LogootStrategy
import raid.neuroide.reproto.crdt.seq.Sequence
import raid.neuroide.reproto.crdt.seq.UniqueSequence
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CrdtListenerChecker<T> {
    private var value: T? = null
    private var called: Int = 0

    fun listener(v: T) {
        value = v
        called++
    }

    fun assertChange(checker: (T) -> Unit): CrdtListenerChecker<T> {
        assertTrue(called > 0)
        assertNotNull(value as Any?)
        checker(value!!)
        return this
    }

    fun assertCalled(): CrdtListenerChecker<T> {
        assertTrue(called > 0)
        return this
    }

    fun assertNotCalled(): CrdtListenerChecker<T> {
        assertEquals(0, called)
        return this
    }

    fun assertOnce(): CrdtListenerChecker<T> {
        assertEquals(1, called)
        return this
    }

    fun reset() {
        called = 0
    }
}

class CrdtTestBuilder(private val upstream: InternalBroadcast) {
    private var siteId = 0

    fun lwwRegister(payload: String = "") = LWWRegister(payload, LocalSiteId(siteId++.toString()))
        .apply {
            upstream.addCrdt(this)
        }

    fun logoot() = Sequence(LocalSiteId(siteId++.toString()), LogootStrategy)
        .apply {
            upstream.addCrdt(this)
        }

    fun useq() = UniqueSequence(LocalSiteId(siteId++.toString()), LogootStrategy)
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
    val UniqueSequence.string
        get() = elements.joinToString("")

    fun <T> ObservableCrdt<T>.listen(): CrdtListenerChecker<T> {
        return CrdtListenerChecker<T>().also { setListener(it::listener) }
    }
}

inline fun crdtTest(func: CrdtTestBuilder.() -> Unit) {
    CrdtTestBuilder(InternalBroadcast()).func()
}

inline fun crdtTest(upstream: InternalBroadcast, func: CrdtTestBuilder.() -> Unit) {
    CrdtTestBuilder(upstream).func()
}
