package raid.neuroide.reproto

interface Gateway

interface ChangesGateway : Gateway {
    fun subscribe(processor: UpdateProcessor)
    fun requestSync(vectorTimestamp: String)
    fun publishUpdate(update: String)
}

interface ClientGateway : ChangesGateway {
    fun loadAndSubscribe(id: String)
    fun setReceiver(receiver: PrototypeReceiver)
}

interface LoadGateway : Gateway {
    suspend fun load(id: String): String?
}

interface StoreGateway : Gateway {
    suspend fun store(id: String, prototype: String)
}

interface UpdateProcessor {
    fun process(update: String)
}

interface PrototypeReceiver {
    fun receivePrototype(id: String, proto: String?)
}
