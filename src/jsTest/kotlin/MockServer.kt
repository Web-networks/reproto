package raid.neuroide.reproto

private typealias PrototypeReceiver = (String, String?) -> Unit
private typealias UpdateProcessor = (String) -> Unit

class MockServer : ClientGateway {
    val context = DefaultContext("").wrapped()
    private val receivers: MutableList<PrototypeReceiver> = mutableListOf()
    private val processors: MutableList<UpdateProcessor> = mutableListOf()
    private val prototypes: MutableMap<String, String> = mutableMapOf()

    private val pSerializer = SerializationManager(context)

    override fun setReceiver(receiver: PrototypeReceiver) {
        receivers.add(receiver)
    }

    override fun subscribe(processor: UpdateProcessor) {
        processors.add(processor)
    }

    override fun load(id: String) {
        val proto = getPrototype(id)
        for (receiver in receivers) {
            receiver(id, proto)
        }
    }

    override fun publishUpdate(update: String) {
        for (subscriber in processors) {
            subscriber(update)
        }
    }

    private fun getPrototype(id: String) =
        prototypes.getOrPut(id) {
            pSerializer.serialize(Prototype(context))
        }

    override fun requestSync(sinceRevision: String) {
        // not implemented
    }
}
