package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.Operation
import kotlin.js.JsName

class ClientNode(site: String) {
    private val context = DefaultContext(site)
    private val upstream = Upstream()
    private val uSerializer = UpdateSerializationManager(context)
    private val pSerializer = PrototypeSerializationManager(context)

    private lateinit var gateway: ClientGateway

    private var currentPrototype: Prototype? = null
    private var currentPrototypeId: String? = null
    private var requestedPrototypeId: String? = null
    private val pendingPrototypeCallbacks: MutableList<(Prototype?) -> Unit> = mutableListOf()

    @JsName("getPrototype")
    fun getPrototype(id: String, callback: (Prototype?) -> Unit) {
        if (currentPrototypeId == id) {
            callback(currentPrototype)
        } else {
            if (requestedPrototypeId != id) {
                pendingPrototypeCallbacks.clear()
                requestedPrototypeId = id
                pendingPrototypeCallbacks.add(callback)
                gateway.loadAndSubscribe(id)
            } else {
                pendingPrototypeCallbacks.add(callback)
            }
        }
    }

    @JsName("setGateway")
    fun setGateway(g: ClientGateway) {
        g.subscribe { update ->
            val upd = uSerializer.deserialize(update)
            processUpdate(upd)
        }
        g.setReceiver { id, proto ->
            val prototype = proto?.let { pSerializer.deserialize(it) }
            receivePrototype(id, prototype)
        }
        gateway = g
    }

    private fun processUpdate(update: Update) {
        if (!update.id.hasNext)
            return
        if (currentPrototypeId != update.id.shift())
            return

        currentPrototype?.processUpdate(update)
    }

    private fun receivePrototype(id: String, proto: Prototype?) {
        if (id != requestedPrototypeId)
            return

        currentPrototype = proto
        currentPrototypeId = requestedPrototypeId
        proto?.setUpstream(upstream.child(id))
        requestedPrototypeId = null

        for (callback in pendingPrototypeCallbacks) {
            callback(proto)
        }
        pendingPrototypeCallbacks.clear()
    }

    private inner class Upstream : ChainedUpstream() {
        override fun process(id: IdChain, op: Operation) {
            val update = (currentPrototype ?: return).log.issueLocalUpdate(id, UpdatePayload(op))
            val serialized = uSerializer.serialize(update)
            gateway.publishUpdate(serialized)
        }
    }
}
