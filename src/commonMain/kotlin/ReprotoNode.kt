//package raid.neuroide.reproto
//
//import kotlinx.coroutines.*
//
//typealias PrototypeIdFilter = (String) -> Boolean
//
//object IdFilters {
//    fun enableAll(id: String) = true
//}
//
//
//// TODO: different versions for client and service
//// No need in uniform variant
//class ReprotoNode {
//    private var idFilter: PrototypeIdFilter = IdFilters::enableAll
//    private val prototypes: PrototypeLibrary = PrototypeLibrary(::requestPrototype)
//    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
//    private val iGateways: MutableList<InitialGateway> = mutableListOf()
//    private val cGateways: MutableSet<ChangesGateway> = mutableSetOf()
//
//    suspend fun getPrototype(id: String): Prototype? {
//        if (!idFilter(id))
//            return null
//        return prototypes.requestPrototype(id)
//    }
//
//    fun getPrototypeAsync(id: String, callback: (Prototype?) -> Unit) {
//        coroutineScope.launch {
//            callback(getPrototype(id))
//        }
//    }
//
//    fun setFilter(filter: PrototypeIdFilter) {
//        idFilter = filter
//    }
//
//    fun addGateway(g: ChangesGateway) {
//        if (cGateways.add(g)) {
//            g.subscribe(processor)
//        }
//    }
//
//    fun addGateway(g: InitialGateway) {
//        iGateways.add(g)
//    }
//
//    private suspend fun requestPrototype(id: String): Prototype? {
//        for (g in iGateways) {
//            val serialized = g.load(id)
//            if (serialized != null)
//                return PrototypeSerializationManager.deserialize(serialized)
//        }
//        return null
//    }
//
//    private fun processUpdate(update: Update) {
//        if (!update.id.hasNext)
//            return
//
//        coroutineScope.launch {
//            getPrototype(update.id.shift())?.processUpdate(update)
//        }
//    }
//
//    private val processor = Processor()
//
//    inner class Processor : UpdateProcessor {
//        override fun process(update: String) {
//            val upd = UpdateSerializationManager.deserialize(update)
//            processUpdate(upd)
//        }
//    }
//}
