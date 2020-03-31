package raid.neuroide.reproto

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ServiceNode(site: String, inMemoryThreshold: Int) : LoadGateway {
    private val context = DefaultContext(site)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val uSerializer = UpdateSerializationManager(context)
    private val pSerializer = PrototypeSerializationManager(context)

    private val cGateways: MutableList<ChangesGateway> = mutableListOf()
    private val lGateways: MutableList<LoadGateway> = mutableListOf()
    private val sGateways: MutableList<StoreGateway> = mutableListOf()
    private val prototypes = PreemptiveContainer(inMemoryThreshold, true, ::requestPrototype)

    fun createPrototype(): String {
        val proto = Prototype(context.wrapped())
        val id = context.issueId()
        val serialized = pSerializer.serialize(proto)
        sGateways.performParallelIo { g ->
            g.store(id, serialized)
        }
        prototypes.putIfAbsent(id, proto)
        return id
    }

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
    }

    override suspend fun load(id: String): String? {
        var res: String? = null
        prototypes.use(id) {
            res = it?.let {
                pSerializer.serialize(it)
            }
        }
        return res
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

    private fun processUpdate(update: Update, serialized: String, sourceGateway: Gateway) {
        if (!update.id.hasNext)
            return

        val id = update.id.shift()
        // TODO: do I need async?
        coroutineScope.launch {
            prototypes.use(id) {
                if (it == null)
                    return@use
                it.processUpdate(update)
                storePrototype(id, it, sourceGateway)
            }
        }
        // TODO: work with ReplicatedLog?
        for (g in cGateways) {
            if (g == sourceGateway)
                continue
            g.publishUpdate(serialized)
        }
    }

    private fun storePrototype(id: String, prototype: Prototype, exceptGateway: Gateway) {
        val serialized = pSerializer.serialize(prototype)
        sGateways.filter { it != exceptGateway }.performParallelIo {
            it.store(id, serialized)
        }
    }

    private inner class Processor(private val g: Gateway) : UpdateProcessor {
        override fun process(update: String) {
            val upd = uSerializer.deserialize(update)
            processUpdate(upd, update, g)
        }
    }

    private fun <T, K> Iterable<T>.performParallelIo(func: suspend (T) -> K): List<K> {
        val defs = map {
            coroutineScope.async {
                func(it)
            }
        }
        return runBlocking(Dispatchers.Unconfined) {
            defs.awaitAll()
        }
    }
}
