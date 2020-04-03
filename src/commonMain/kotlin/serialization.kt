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
                contextual(NodeContextWrapper::class, ContextualInjectorSerializer(context.wrapped()))
            }

internal class PrototypeSerializationManager(context: NodeContext) {
    private val module = getSerialModuleForContext(context)

    fun serialize(prototype: Prototype): String {
        return getJson().stringify(Prototype.serializer(), prototype)
    }

    fun deserialize(data: String): Prototype {
        return getJson().parse(Prototype.serializer(), data)
    }

    private fun getJson() = Json(JsonConfiguration.Stable.copy(classDiscriminator = "_type"), module)
}

internal class UpdateSerializationManager(context: NodeContext) {
    private val module = getSerialModuleForContext(context)

    fun serialize(update: Update): String {
        return getJson().stringify(Update.serializer(), update)
    }

    fun deserialize(data: String): Update {
        return getJson().parse(Update.serializer(), data)
    }

    private fun getJson() = Json(JsonConfiguration.Stable.copy(classDiscriminator = "_type"), module)
}
