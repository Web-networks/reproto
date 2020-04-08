package raid.neuroide.reproto

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import raid.neuroide.reproto.crdt.LocalSiteId
import raid.neuroide.reproto.crdt.VectorTimestamp
import raid.neuroide.reproto.crdt.getCrdtSerializers


private fun getSerialModuleForContext(siteId: LocalSiteId) =
    getCrdtSerializers(siteId)

internal class SerializationManager(siteId: LocalSiteId) {
    private val module = getSerialModuleForContext(siteId)

    fun serialize(prototype: Prototype): String {
        return getJson().stringify(Prototype.serializer(), prototype)
    }

    fun deserializePrototype(data: String): Prototype {
        return getJson().parse(Prototype.serializer(), data)
    }

    fun serialize(update: Update): String {
        return getJson().stringify(Update.serializer(), update)
    }

    fun deserializeUpdate(data: String): Update {
        return getJson().parse(Update.serializer(), data)
    }

    fun serialize(vectorTimestamp: VectorTimestamp): String {
        return getJson().stringify(VectorTimestamp.serializer(), vectorTimestamp)
    }

    private fun getJson() = Json(JsonConfiguration.Stable.copy(classDiscriminator = "_type"), module)
}
