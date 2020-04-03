package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.VectorTimestamp

internal class LogStorage(
    private val gateway: LogStorageGateway,
    private val inMemoryThreshold: Int,
    private val serializer: UpdateSerializationManager
) {
    // TODO: cache updates in memory
    // TODO: use more efficient serialization
    fun save(prototypeId: String, update: Update) {
        gateway.save(prototypeId, update.toEntry())
    }

    fun restore(prototypeId: String, sinceRevision: VectorTimestamp, maxCount: Int): List<Update>? {
        return gateway.restore(prototypeId, sinceRevision.times, maxCount).map {
            it.toUpdate()
        }
    }

    private fun Update.toEntry(): LogStorageGateway.Entry {
        return LogStorageGateway.Entry(index.site, index.time.toInt(), serializer.serialize(this))
    }

    private fun LogStorageGateway.Entry.toUpdate(): Update {
        return serializer.deserialize(payload)
    }
}
