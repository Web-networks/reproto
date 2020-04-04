package raid.neuroide.reproto

interface Gateway

interface LoadGateway : Gateway {
    suspend fun load(id: String): String?
}

interface StoreGateway : Gateway {
    suspend fun store(id: String, prototype: String)
}

interface ChangesGateway : Gateway {
    //TODO
    //    fun requestSync(vectorTimestamp: VectorTimestamp)
    suspend fun publishUpdate(update: String)
}

interface LogStorageGateway {
    data class Entry(val site: String, val originIndex: Int, val payload: String)

    fun save(prototypeId: String, entry: Entry)
    fun restore(prototypeId: String, sinceRevision: Map<String, Int>, maxCount: Int): List<Entry>
}
