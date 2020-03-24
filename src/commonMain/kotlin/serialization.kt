@file:OptIn(kotlinx.serialization.UnstableDefault::class)

package raid.neuroide.reproto

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import raid.neuroide.reproto.common.ContextualInjectorSerializer
import raid.neuroide.reproto.crdt.getCrdtSerializers

private fun getSerialModuleForContext(context: NodeContext) =
    getCrdtSerializers(context.siteId) +
            SerializersModule {
                contextual(NodeContext::class, ContextualInjectorSerializer(context))
            }

internal class PrototypeSerializationManager(private val context: NodeContext) {
    private val module = getSerialModuleForContext(context)

    fun serialize(prototype: Prototype): String {
        return Json(JsonConfiguration.Default, module).stringify(Prototype.serializer(), prototype)
    }

    fun deserialize(data: String): Prototype {
        return Json(JsonConfiguration.Default, module).parse(Prototype.serializer(), data)
    }
}

internal class UpdateSerializationManager(context: NodeContext) {
    private val module = getSerialModuleForContext(context)

    fun serialize(update: Update): String {
        return Json(JsonConfiguration.Default, module).stringify(Update.serializer(), update)
    }

    fun deserialize(data: String): Update {
        return Json(JsonConfiguration.Default, module).parse(Update.serializer(), data)
    }
}
