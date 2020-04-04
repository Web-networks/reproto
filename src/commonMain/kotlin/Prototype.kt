package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import raid.neuroide.reproto.crdt.seq.LogootStrategy
import raid.neuroide.reproto.crdt.seq.Sequence
import kotlin.js.JsName

@Serializable
class Prototype constructor(private val context: NodeContextWrapper) {
    private val layersMap: MutableMap<String, Layer> = mutableMapOf()
    internal val log = ReplicatedLog(context)

    @Transient
    private var myUpstream: ChainedUpstream? = null

    // this safe call is necessary to overcome a bug in kotlinx.serialization
    @Suppress("UNNECESSARY_SAFE_CALL")
    private val layerSequence = Sequence(this?.context?.siteId, LogootStrategy)

    @JsName("layers")
    val layers: Array<Layer>
        get() = layerSequence.content.map { id ->
            layersMap.getOrPut(id) { createLayer(id) }
        }.toTypedArray()

    @JsName("addLayer")
    fun addLayer(position: Int): Layer {
        val layerId = context.issueId()
        val layer = createLayer(layerId)

        layerSequence.insert(position, layerId)
        layersMap[layerId] = layer
        return layer
    }

    @JsName("moveLayer")
    fun moveLayer(from: Int, to: Int) {
        layerSequence.move(from, to)
    }

    @JsName("removeLayer")
    fun removeLayer(position: Int) {
        layerSequence.delete(position)
        // TODO: delete from map (on all nodes!)
    }

    internal fun setUpstream(upstream: ChainedUpstream) {
        myUpstream = upstream
        layerSequence.setUpstream(upstream)
        for ((layerId, layer) in layersMap) {
            layer.setUpstream(upstream.child(layerId))
        }
    }

    internal fun processUpdate(update: Update) {
        if (!log.tryCommit(update))
            return

        if (update.id.hasNext) {
            val layerId = update.id.shift()
            layersMap.getOrPut(layerId) {
                createLayer(layerId)
            }.processUpdate(update)
        } else {
            applyUpdate(update.payload)
        }
    }

    private fun createLayer(id: String): Layer {
        val layer = Layer(context)
        myUpstream?.let { layer.setUpstream(it.child(id)) }
        return layer
    }

    private fun applyUpdate(update: UpdatePayload) {
        layerSequence.deliver(update.operation)
    }
}
