package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId

class MockServer : ClientGateway {
    val context = Context().wrapped()
    private val receivers: MutableList<PrototypeReceiver> = mutableListOf()
    private val processors: MutableList<UpdateProcessor> = mutableListOf()
    private val prototypes: MutableMap<String, String> = mutableMapOf()

    private val pSerializer = PrototypeSerializationManager(context)

    override fun setReceiver(receiver: PrototypeReceiver) {
        receivers.add(receiver)
    }

    override fun subscribe(processor: UpdateProcessor) {
        processors.add(processor)
    }

    override fun loadAndSubscribe(id: String) {
        val proto = getPrototype(id)
        for (receiver in receivers) {
            receiver.receivePrototype(id, proto)
        }
    }

    override fun publishUpdate(update: String) {
        for (subscriber in processors) {
            subscriber.process(update)
        }
    }

    private fun getPrototype(id: String) =
        prototypes.getOrPut(id) {
            pSerializer.serialize(Prototype(context))
        }

    override fun requestSync(vectorTimestamp: String) {
        throw NotImplementedError()
    }

    private inner class Context : NodeContext {
        private var idCounter = 0

        override val siteId = LocalSiteId("")

        override fun issueId(): String {
            idCounter++
            return "$idCounter"
        }
    }
}