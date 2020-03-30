package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.Operation
import raid.neuroide.reproto.crdt.Upstream


internal abstract class ChainedUpstream : Upstream {
    abstract fun process(id: IdChain, op: Operation)

    override fun deliver(op: Operation) {
        process(IdChain(), op)
    }

    fun child(additionalId: String): ChainedUpstream = object : ChainedUpstream() {
        override fun process(id: IdChain, op: Operation) {
            this@ChainedUpstream.process(IdChain(id.chain + listOf(additionalId)), op)
        }
    }
}
