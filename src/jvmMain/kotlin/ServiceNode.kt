package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.VectorTimestamp

class ServiceNode(
    site: String,
    inMemoryThreshold: Int,
    idCounter: PersistentValue<Long> = InMemoryValue()
) {
    private val context = ServiceContext(site, idCounter)
    private val serializer = SerializationManager(context)

    private val cGateways: MutableList<ChangesGateway> = mutableListOf()
    private val psGateways: MutableList<PrototypeStorageGateway> = mutableListOf()
    private var logStorage: LogStorage? = null

    private val prototypes = PreemptiveContainer(inMemoryThreshold, true, ::requestPrototype)

    fun addGateway(g: Gateway) {
        if (g is PrototypeStorageGateway) {
            psGateways.add(g)
        }
        if (g is ChangesGateway) {
            cGateways.add(g)
        }
        if (g is LogStorageGateway) {
            logStorage = createLogStorage(g)
        }
    }

    private fun createLogStorage(g: LogStorageGateway): LogStorage? {
        return LogStorage(g, 5000, serializer)
    }

    fun createPrototype(): String {
        val proto = Prototype(context.wrapped())
        val id = context.issueId()
        val serialized = serializer.serialize(proto)
        psGateways.forEach { g ->
            g.store(id, serialized)
        }
        initPrototype(id, proto)
        prototypes.putIfAbsent(id, proto)
        return id
    }

    suspend fun load(id: String): String? {
        return prototypes.use(id) {
            it?.let {
                serializer.serialize(it)
            }
        }
    }

    suspend fun getUpdates(id: String, sinceRevision: VectorTimestamp, maxCount: Int): List<String>? {
        // possible optimizations: don't actually fetch the model
        return prototypes.use(id) {
            it?.log?.getUpdates(sinceRevision, maxCount)
        }?.map {
            serializer.serialize(it)
        }
    }

    suspend fun postUpdate(update: String) {
        val upd = serializer.deserializeUpdate(update)
        processUpdate(upd, update)
    }

    private suspend fun requestPrototype(id: String): Prototype? {
        for (g in psGateways) {
            val serialized = g.load(id)
            if (serialized != null) {
                val proto = serializer.deserializePrototype(serialized)
                initPrototype(id, proto)
                return proto
            }
        }
        return null
    }

    private fun initPrototype(id: String, proto: Prototype) {
        proto.log.setUpstream(LogStorageUpstream(id))
    }

    private suspend fun processUpdate(update: Update, serialized: String) {
        if (!update.id.hasNext)
            return

        val id = update.id.shift()
        prototypes.use(id) {
            if (it == null)
                return@use
            it.processUpdate(update)
            storePrototype(id, it)
        }
        // TODO: work with ReplicatedLog?
        for (g in cGateways) {
            g.publishUpdate(serialized)
        }
    }

    private fun storePrototype(id: String, prototype: Prototype) {
        val serialized = serializer.serialize(prototype)
        psGateways.forEach {
            it.store(id, serialized)
        }
    }

    private inner class LogStorageUpstream(private val prototypeId: String) : LogUpstream {
        override fun save(update: Update) {
            logStorage?.save(prototypeId, update)
        }

        override fun restore(sinceRevision: VectorTimestamp, maxCount: Int): List<Update>? {
            return logStorage?.restore(prototypeId, sinceRevision, maxCount)
        }
    }
}
