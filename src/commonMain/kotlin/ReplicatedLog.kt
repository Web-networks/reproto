package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import raid.neuroide.reproto.crdt.LamportTimestamp
import raid.neuroide.reproto.crdt.LocalSiteId
import raid.neuroide.reproto.crdt.VectorClock
import raid.neuroide.reproto.crdt.VectorTimestamp


@Serializable
class ReplicatedLog(@Suppress("CanBeParameter") private val context: NodeContextWrapper) {
    @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS") // to bypass the bug in kotlinx.serialization
    private val clock = VectorClock(this?.context?.siteId ?: LocalSiteId(""))

    @Suppress("UNNECESSARY_SAFE_CALL") // to bypass the bug in kotlinx.serialization
    @Transient
    private val site = this?.context?.siteId?.id

    val currentTimestamp: VectorTimestamp
        get() = clock.value

    fun issueLocalUpdate(id: IdChain, payload: UpdatePayload): Update {
        return Update(id, issueLocalTimestamp(), payload)
    }

    /**
     * We make strong assumptions about the network channel to provide causal delivery.
     * Namely, it must not reorder messages.
     * If a message is lost for any reason it must be redelivered before the next messages are sent.
     *
     * Enforcing causal delivery without such assumptions will require transfer of vector clocks.
     * To avoid it, this implementation does not actually check for reorders.
     *
     * @return true if the message can be accepted
     * @throws IllegalStateException if some messages were definitely lost. Don't rely on this check.
     */
    fun tryDeliver(update: Update): Boolean {
        val (longIndex, origin) = update.index
        val index = longIndex.toInt()
        val currentIndex = clock.value[origin]

        if (index <= currentIndex) {
            return false
        }
        if (index > currentIndex + 1) {
            throw IllegalStateException("Messages were lost")
        }

        clock.update(VectorTimestamp(mapOf(origin to index)))
        return true
    }

    private fun issueLocalTimestamp(): LamportTimestamp {
        val localIndex = clock.next()[site]
        return LamportTimestamp(localIndex.toLong(), site)
    }
}
