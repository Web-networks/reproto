package raid.neuroide.reproto

import kotlinx.serialization.Serializable

@Serializable
class Prototype : ContextReceiver() {
    private val layers: MutableMap<String, Layer> = mutableMapOf()

    override fun setContext(context: NodeContext) {
        super.setContext(context)
        for (layer in layers.values) {
            layer.setContext(context)
        }
    }

    fun processUpdate(update: Update) {
        if (update.id.hasNext) {
            layers.getOrPut(update.id.shift(), ::createLayer).processUpdate(update)
        } else {
            applyUpdate(update.payload)
        }
    }

    private fun createLayer(): Layer {
        val layer = Layer()
        layer.setContext(myContext)
        return layer
    }

    private fun applyUpdate(update: UpdatePayload) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
