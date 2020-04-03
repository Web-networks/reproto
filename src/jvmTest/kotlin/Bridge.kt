package raid.neuroide.reproto

import kotlinx.coroutines.runBlocking

private typealias PrototypeReceiver = (String, String?) -> Unit

class Bridge(private val service: ServiceNode) {
    private val clientProcessors: MutableList<(String) -> Unit> = mutableListOf()
    private val serviceGateway = ServiceGateway()

    init {
        service.addGateway(serviceGateway)
    }

    fun newClientGateway(): ClientGateway = ClientGatewayImpl()

    private inner class ServiceGateway : ChangesGateway {
        val processors: MutableList<UpdateProcessor> = mutableListOf()

        override fun subscribe(processor: UpdateProcessor) {
            processors.add(processor)
        }

        override suspend fun publishUpdate(update: String) {
            clientProcessors.forEach {
                it(update)
            }
        }
    }

    private inner class ClientGatewayImpl : ClientGateway {
        private var myReceiver: PrototypeReceiver? = null

        override fun loadAndSubscribe(id: String) {
            val proto = runBlocking { service.load(id) }
            myReceiver?.invoke(id, proto)
        }

        override fun setReceiver(receiver: PrototypeReceiver) {
            myReceiver = receiver
        }

        override fun subscribe(processor: (String) -> Unit) {
            clientProcessors.add(processor)
        }

        override fun requestSync(vectorTimestamp: String) {
            throw NotImplementedError()
        }

        override fun publishUpdate(update: String) = runBlocking {
            serviceGateway.processors.forEach {
                it.process(update)
            }
        }
    }
}
