package raid.neuroide.reproto

interface Gateway

interface LoadGateway : Gateway {
    suspend fun load(id: String): String?
}

interface StoreGateway : Gateway {
    suspend fun store(id: String, prototype: String)
}

interface ChangesGateway : Gateway {
    fun subscribe(processor: UpdateProcessor)
    //TODO
    //    fun requestSync(vectorTimestamp: VectorTimestamp)
    suspend fun publishUpdate(update: String)
}

interface UpdateProcessor {
    suspend fun process(update: String)
}
