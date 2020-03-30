package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import raid.neuroide.reproto.crdt.LWWRegister
import raid.neuroide.reproto.crdt.Operation
import raid.neuroide.reproto.crdt.RegisterWrapper

@Serializable
class Layer constructor(private val context: NodeContextWrapper) {
    private val parameters: MutableMap<String, LWWRegister> = mutableMapOf()

    @Transient
    private var myUpstream: ChainedUpstream? = null

    operator fun get(paramName: String): RegisterWrapper {
        val rg = parameters.getOrPut(paramName) { createRegister(paramName) }
        return RegisterWrapper(rg)
    }

    internal fun setUpstream(upstream: ChainedUpstream) {
        myUpstream = upstream
        for (param in parameters.values) {
            param.setUpstream(upstream)
        }
    }

    internal fun processUpdate(update: Update) {
        if (update.id.hasNext) {
            applyUpdate(update.id.shift(), update.payload.operation)
        } else {
            applyUpdate(update.payload)
        }
    }

    private fun createRegister(paramName: String): LWWRegister {
        val rg = LWWRegister("", context.siteId)
        myUpstream?.let { rg.setUpstream(it.child(paramName)) }
        return rg
    }

    private fun applyUpdate(paramName: String, operation: Operation) {
        parameters.getOrPut(paramName) { createRegister(paramName) }.deliver(operation)
    }

    private fun applyUpdate(update: UpdatePayload) {
        // TODO: maybe don't throw?
        throw UnsupportedOperationException()
    }
}
