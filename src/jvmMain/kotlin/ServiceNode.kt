package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.VectorTimestamp

class ServiceNode(site: String, inMemoryThreshold: Int) : LoadGateway {
    private val context = DefaultContext(site)
    private val uSerializer = UpdateSerializationManager(context)
    private val pSerializer = PrototypeSerializationManager(context)

    private val cGateways: MutableList<ChangesGateway> = mutableListOf()
    private val lGateways: MutableList<LoadGateway> = mutableListOf()
    private val sGateways: MutableList<StoreGateway> = mutableListOf()
    private var logStorage: LogStorage? = null

    private val prototypes = PreemptiveContainer(inMemoryThreshold, true, ::requestPrototype)

    fun addGateway(g: Gateway) {
        if (g is StoreGateway) {
            sGateways.add(g)
        }
        if (g is LoadGateway) {
            lGateways.add(g)
        }
        if (g is ChangesGateway) {
            g.subscribe(Processor(g))
            cGateways.add(g)
        }
        if (g is LogStorageGateway) {
            logStorage = createLogStorage(g)
        }
    }

    private fun createLogStorage(g: LogStorageGateway): LogStorage? {
        return LogStorage(g, 5000, uSerializer)
    }

    suspend fun createPrototype(): String {
        val proto = Prototype(context.wrapped())
        val id = context.issueId()
        val serialized = pSerializer.serialize(proto)
        sGateways.forEach { g ->
            g.store(id, serialized)
        }
        prototypes.putIfAbsent(id, proto)
        return id
    }

    override suspend fun load(id: String): String? {
        return prototypes.use(id) {
            it?.let {
                pSerializer.serialize(it)
            }
        }
    }

    suspend fun getUpdates(id: String, sinceRevision: VectorTimestamp, maxCount: Int): List<String>? {
        // possible optimizations: don't actually fetch the model
        return prototypes.use(id) {
            it?.log?.getUpdates(sinceRevision, maxCount)
        }?.map {
            uSerializer.serialize(it)
        }
    }

    private suspend fun requestPrototype(id: String): Prototype? {
        for (g in lGateways) {
            val serialized = g.load(id)
            if (serialized != null) {
                val proto = pSerializer.deserialize(serialized)
                // no need in upstream
                // because we don't perform local changes
                return proto
            }
        }
        return null
    }

    private suspend fun processUpdate(update: Update, serialized: String, sourceGateway: Gateway) {
        if (!update.id.hasNext)
            return

        val id = update.id.shift()
        prototypes.use(id) {
            if (it == null)
                return@use
            it.processUpdate(update)
            storePrototype(id, it, sourceGateway)
        }
        // TODO: work with ReplicatedLog?
        for (g in cGateways) {
//            if (g == sourceGateway)
//                continue
            g.publishUpdate(serialized)
        }
    }

    private suspend fun storePrototype(id: String, prototype: Prototype, exceptGateway: Gateway) {
        val serialized = pSerializer.serialize(prototype)
        sGateways/* .filter { it != exceptGateway } */.forEach {
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

    private inner class Processor(private val g: Gateway) : UpdateProcessor {
        override suspend fun process(update: String) {
            val upd = uSerializer.deserialize(update)
            processUpdate(upd, update, g)
        }
    }
}
