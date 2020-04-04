package raid.neuroide.reproto

interface Gateway

interface PrototypeStorageGateway : Gateway {
    suspend fun load(id: String): String?
    suspend fun store(id: String, prototype: String)
}

interface LogStorageGateway : Gateway {
    data class Entry(val site: String, val originIndex: Int, val payload: String)

    fun save(prototypeId: String, entry: Entry)
    fun restore(prototypeId: String, sinceRevision: Map<String, Int>, maxCount: Int): List<Entry>
}

interface ChangesGateway : Gateway {
    //    fun requestSync(vectorTimestamp: VectorTimestamp)
    suspend fun publishUpdate(update: String)
}
