package raid.neuroide.reproto

import kotlin.js.JsName

interface ClientGateway {
    @JsName("load")
    fun load(id: String)
    @JsName("setReceiver")
    fun setReceiver(receiver: (String, String?) -> Unit)
    @JsName("subscribe")
    fun subscribe(processor: (String) -> Unit)
    @JsName("publishUpdate")
    fun publishUpdate(update: String)
    @JsName("requestSync")
    fun requestSync(sinceRevision: String)
}
