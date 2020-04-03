package raid.neuroide.reproto

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private typealias Provider<K, V> = suspend (K) -> V?

class PreemptiveContainer<K, V>(
    private val thresholdCapacity: Int = 1000,
    private val preemptNulls: Boolean = true,
    private val provider: Provider<K, V>
) {
    private val container: ConcurrentMap<K, Holder> = ConcurrentHashMap()
    private val thiningMutex = Mutex()

    suspend fun <R> use(key: K, consumer: suspend (V?) -> R): R {
        val holder = container.compute(key) { _, v ->
            if (v == null || !v.acquire()) {
                Holder(key).apply { acquire() }
            } else v
        }!!

        val valueIsNull: Boolean
        val free: Boolean
        var result: R
        try {
            holder.use(consumer).run {
                valueIsNull = !first
                result = second
            }
        } finally {
            free = holder.release()
        }

        if (preemptNulls && valueIsNull && free) {
            holder.tryPreempt()
        }
        if (container.size >= thresholdCapacity) {
            thin()
        }

        return result
    }

    fun putIfAbsent(key: K, value: V) {
        container.computeIfAbsent(key) {
            Holder(key).apply {
                acquire() // to bump timestamp
                runBlocking {
                    putValueIfAbsent(value)
                }
                release()
            }
        }

        if (container.size >= thresholdCapacity) {
            thin()
        }
    }

    private fun thinImpl() {
        if (container.size < thresholdCapacity)
            return

        val targetCapacity = thresholdCapacity / 2
        val entries = container.entries.sortedBy { it.value.lastUseTime }

        var count = entries.size
        for (e in entries) {
            if (e.value.tryPreempt() && container.remove(e.key, e.value)) {
                --count
            }

            if (count <= targetCapacity)
                break
        }
    }

    private fun thin() {
        if (!thiningMutex.tryLock())
            return

        try {
            thinImpl()
        } finally {
            thiningMutex.unlock()
        }
    }

    private inner class Holder(private val key: K) {
        val lastUseTime: Long
            get() = lastAcquiredTimestamp.get()

        private val refCounter = AtomicInteger(0)
        private val lastAcquiredTimestamp = AtomicLong(Long.MAX_VALUE)

        private val mutex = Mutex()
        private var value: V? = null
        private var valueComputed: Boolean = false

        /**
         * @return true if value is not null
         */
        suspend inline fun <R> use(crossinline consumer: suspend (V?) -> R): Pair<Boolean, R> =
            mutex.withLock {
                if (!valueComputed) {
                    value = computeValue()
                    valueComputed = true
                }
                val res = consumer(value)
                Pair(value != null, res)
            }

        private suspend fun computeValue(): V? = provider(key)

        suspend fun putValueIfAbsent(v: V) {
            mutex.withLock {
                if (!valueComputed) {
                    value = v
                    valueComputed = true
                }
            }
        }

        fun acquire(): Boolean {
            lastAcquiredTimestamp.set(System.currentTimeMillis())
            return refCounter.incrementAndGet() > 0
        }

        fun release(): Boolean {
            return refCounter.decrementAndGet() == 0
        }

        fun tryPreempt(): Boolean {
            return refCounter.compareAndSet(0, Int.MIN_VALUE) || refCounter.get() < 0
        }
    }
}
