package raid.neuroide.reproto

import kotlinx.coroutines.runBlocking

class Bridge(private val service: ServiceNode) {
    private val clientProcessors: MutableList<UpdateProcessor> = mutableListOf()
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

        override fun requestSync(vectorTimestamp: String) {
            throw NotImplementedError()
        }

        override fun publishUpdate(update: String) {
            clientProcessors.forEach {
                it.process(update)
            }
        }
    }

    private inner class ClientGatewayImpl : ClientGateway {
        private var myReceiver: PrototypeReceiver? = null

        override fun loadAndSubscribe(id: String) {
            val proto = runBlocking { service.load(id) }
            myReceiver?.receivePrototype(id, proto)
        }

        override fun setReceiver(receiver: PrototypeReceiver) {
            myReceiver = receiver
        }

        override fun subscribe(processor: UpdateProcessor) {
            clientProcessors.add(processor)
        }

        override fun requestSync(vectorTimestamp: String) {
            throw NotImplementedError()
        }

        override fun publishUpdate(update: String) {
            serviceGateway.processors.forEach {
                it.process(update)
            }
        }
    }


}