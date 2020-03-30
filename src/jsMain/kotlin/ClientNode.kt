package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId
import raid.neuroide.reproto.crdt.Operation

class ClientNode(site: String) {
    private val context = Context(site)
    private val processor = Processor()
    private val upstream = Upstream()
    private val uSerializer = UpdateSerializationManager(context)
    private val pSerializer = PrototypeSerializationManager(context)

    private lateinit var gateway: ClientGateway

    private var currentPrototype: Prototype? = null
    private var currentPrototypeId: String? = null
    private var requestedPrototypeId: String? = null
    private val pendingPrototypeCallbacks: MutableList<(Prototype?) -> Unit> = mutableListOf()

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

    fun setGateway(g: ClientGateway) {
        g.subscribe(processor)
        g.setReceiver(processor)
        gateway = g
    }

    private fun processUpdate(update: Update) {
        if (!update.id.hasNext)
            return
        if (currentPrototypeId != update.id.shift())
            return

        currentPrototype?.processUpdate(update)
    }

    private fun receivePrototype(id: String, proto: Prototype) {
        if (id != requestedPrototypeId)
            return

        currentPrototype = proto
        currentPrototypeId = requestedPrototypeId
        proto.setUpstream(upstream.child(id))
        requestedPrototypeId = null

        for (callback in pendingPrototypeCallbacks) {
            callback(proto)
        }
        pendingPrototypeCallbacks.clear()
    }

    private inner class Processor : UpdateProcessor,
        PrototypeReceiver {
        override fun process(update: String) {
            val upd = uSerializer.deserialize(update)
            processUpdate(upd)
        }

        override fun receivePrototype(id: String, proto: String) {
            val prototype = pSerializer.deserialize(proto)
            receivePrototype(id, prototype)
        }
    }

    private inner class Context private constructor(override val siteId: LocalSiteId) :
        NodeContext {
        private var idCounter: Int = 0

        constructor(site: String) : this(LocalSiteId(site))

        override fun issueId(): String {
            idCounter++
            return "${siteId.id}::${idCounter}"
        }
    }

    private inner class Upstream : ChainedUpstream() {
        override fun process(id: IdChain, op: Operation) {
            val update = Update(id, UpdatePayload(op))
            val serialized = uSerializer.serialize(update)
            gateway.publishUpdate(serialized)
        }
    }
}
