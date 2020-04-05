package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.RegisterWrapper
import kotlin.js.JsName

interface PrototypeListener {
    @JsName("layerAdded")
    fun layerAdded(index: Int, layer: Layer)
    @JsName("layerRemoved")
    fun layerRemoved(index: Int, layer: Layer)
    @JsName("layerMoved")
    fun layerMoved(from: Int, to: Int, layer: Layer)
    @JsName("parameterChanged")
    fun parameterChanged(layer: Layer, paramName: String, register: RegisterWrapper)
}
