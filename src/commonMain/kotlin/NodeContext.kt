package raid.neuroide.reproto

import kotlinx.serialization.ContextSerializer
import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.LocalSiteId

@OptIn(kotlinx.serialization.ImplicitReflectionSerializer::class) // TODO: use module-wide opt-in
@Serializable(with = ContextSerializer::class)
interface NodeContext {
    val siteId: LocalSiteId
}
