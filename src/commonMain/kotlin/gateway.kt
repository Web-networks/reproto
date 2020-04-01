package raid.neuroide.reproto

import kotlin.js.JsName

interface Gateway

interface ChangesGateway : Gateway {
    @JsName("subscribe")
    fun subscribe(processor: UpdateProcessor)
    @JsName("requestSync")
    fun requestSync(vectorTimestamp: String)
    @JsName("publishUpdate")
    fun publishUpdate(update: String)
}

interface ClientGateway : ChangesGateway {
    @JsName("loadAndSubscribe")
    fun loadAndSubscribe(id: String)
    @JsName("setReceiver")
    fun setReceiver(receiver: PrototypeReceiver)
}

interface LoadGateway : Gateway {
    suspend fun load(id: String): String?
}

interface StoreGateway : Gateway {
    suspend fun store(id: String, prototype: String)
}

interface UpdateProcessor {
    @JsName("process")
    fun process(update: String)
}

interface PrototypeReceiver {
    @JsName("receivePrototype")
    fun receivePrototype(id: String, proto: String?)
}
