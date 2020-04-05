package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.RegisterWrapper

interface PrototypeListener {
    fun layerAdded(index: Int, layer: Layer)
    fun layerRemoved(index: Int, layer: Layer)
    fun layerMoved(from: Int, to: Int, layer: Layer)
    fun parameterChanged(layer: Layer, paramName: String, register: RegisterWrapper)
}
