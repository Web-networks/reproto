package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.LWWRegister
import raid.neuroide.reproto.crdt.LamportClock
import raid.neuroide.reproto.crdt.Operation

@Serializable
class Layer : ContextReceiver() {
    private val parameters: MutableMap<String, LWWRegister> = mutableMapOf()

    fun processUpdate(update: Update) {
        if (update.id.hasNext) {
            applyUpdate(update.id.shift(), update.payload.operation)
        } else {
            applyUpdate(update.payload)
        }
    }

    private fun applyUpdate(paramName: String, operation: Operation) {
        parameters.getOrPut(paramName) {
            LWWRegister("", LamportClock(myContext.site))
        }.deliver(operation)
    }

    private fun applyUpdate(update: UpdatePayload) {
        // TODO
    }
}
