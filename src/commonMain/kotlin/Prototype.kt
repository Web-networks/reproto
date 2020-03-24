package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.seq.LogootStrategy
import raid.neuroide.reproto.crdt.seq.Sequence

@Serializable
class Prototype(private val context: NodeContext) {
    private val layers: MutableMap<String, Layer> = mutableMapOf()
    private val layerSequence = Sequence(context.siteId, LogootStrategy)

    internal fun processUpdate(update: Update) {
        if (update.id.hasNext) {
            layers.getOrPut(update.id.shift(), ::createLayer).processUpdate(update)
        } else {
            applyUpdate(update.payload)
        }
    }

    private fun createLayer(): Layer {
        val layer = Layer(context)
        return layer
    }

    private fun applyUpdate(update: UpdatePayload) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
