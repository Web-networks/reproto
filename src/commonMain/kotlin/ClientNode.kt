package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.Operation
import raid.neuroide.reproto.crdt.VectorTimestamp
import kotlin.js.JsName

class ClientNode(site: String, idCounterInitial: Int = 0) {
    private val context = DefaultContext(site, idCounterInitial)
    private val upstream = ChainedUpstreamBud(::processSerializedUpdate)
    private val logUpstream = LogSyncUpstream()
    private val serializer = SerializationManager(context)

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
                gateway.load(id)
            } else {
                pendingPrototypeCallbacks.add(callback)
            }
        }
    }

    @JsName("setGateway")
    fun setGateway(g: ClientGateway) {
        g.subscribe { update ->
            val upd = serializer.deserializeUpdate(update)
            processUpdate(upd)
        }
        g.setReceiver { id, proto ->
            val prototype = proto?.let { serializer.deserializePrototype(it) }
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
        requestedPrototypeId = null

        proto?.apply {
            setUpstream(upstream.child(id))
            log.setUpstream(logUpstream)
            gateway.requestSync(id, serializer.serialize(log.currentTimestamp))
        }

        for (callback in pendingPrototypeCallbacks) {
            callback(proto)
        }
        pendingPrototypeCallbacks.clear()
    }

    private fun processSerializedUpdate(id: IdChain, op: Operation) {
        val update = (currentPrototype ?: return).log.issueLocalUpdate(id, UpdatePayload(op))
        val serialized = serializer.serialize(update)
        gateway.publishUpdate(serialized)
    }

    private inner class LogSyncUpstream : LogUpstream {
        override fun save(update: Update) {
            currentPrototype?.let {
                gateway.requestSync(currentPrototypeId!!, serializer.serialize(it.log.currentTimestamp))
            }
        }

        override fun restore(sinceRevision: VectorTimestamp, maxCount: Int): List<Update>? {
            return null
        }
    }
}
