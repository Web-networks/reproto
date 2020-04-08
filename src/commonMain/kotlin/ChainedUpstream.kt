package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.Operation
import raid.neuroide.reproto.crdt.Upstream

private typealias UpstreamProcessor = (id: IdChain, op: Operation) -> Unit

internal class ChainedUpstreamBud(
    private val processor: UpstreamProcessor,
    private val id: IdChain = IdChain()
) {
    fun deliver(op: Operation) {
        processor(id, op)
    }

    fun child(indexIssuer: () -> Long): ChainedUpstream =
        ChainedUpstream(processor, id, indexIssuer)

    fun child(additionalId: String): ChainedUpstreamBud =
        ChainedUpstreamBud(processor, id + additionalId)
}

internal class ChainedUpstream(
    private val processor: UpstreamProcessor,
    private val id: IdChain,
    private val indexIssuer: () -> Long
) : Upstream {

    override fun deliver(op: Operation) {
        processor(id, op)
    }

    override fun nextLocalIndex(): Long = indexIssuer()

    fun child(additionalId: String): ChainedUpstream =
        ChainedUpstream(processor, id + additionalId, indexIssuer)
}
