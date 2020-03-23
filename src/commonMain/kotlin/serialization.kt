package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.Operation

object PrototypeSerializationManager {
    fun serialize(prototype: Prototype): String {
        return ""
    }

    fun deserialize(data: String): Prototype {
        return Prototype()
    }
}

object UpdateSerializationManager {
    fun serialize(update: Update): String {
        return ""
    }

    fun deserialize(data: String): Update {
        return Update(IdChain(), UpdatePayload(object : Operation {}))
    }
}
