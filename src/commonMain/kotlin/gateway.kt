package raid.neuroide.reproto

interface ChangesGateway {
    fun subscribe(processor: UpdateProcessor)
    fun requestSync(vectorTimestamp: String)
    fun publishUpdate(update: String)
}

interface ClientGateway : ChangesGateway {
    fun loadAndSubscribe(id: String)
    fun setReceiver(receiver: PrototypeReceiver)
}

interface UpdateProcessor {
    fun process(update: String)
}

interface PrototypeReceiver {
    fun receivePrototype(id: String, proto: String)
}
