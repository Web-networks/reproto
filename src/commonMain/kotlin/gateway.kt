package raid.neuroide.reproto

import kotlin.js.JsName

interface ClientGateway {
    @JsName("loadAndSubscribe")
    fun loadAndSubscribe(id: String)
    @JsName("setReceiver")
    fun setReceiver(receiver: (String, String?) -> Unit)
    @JsName("subscribe")
    fun subscribe(processor: (String) -> Unit)
    @JsName("requestSync")
    fun requestSync(vectorTimestamp: String)
    @JsName("publishUpdate")
    fun publishUpdate(update: String)
}
