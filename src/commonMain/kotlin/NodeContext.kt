package raid.neuroide.reproto

import kotlinx.serialization.ContextSerializer
import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.LocalSiteId

interface NodeContext {
    val siteId: LocalSiteId
    fun issueId(): String
}

@Serializable(with = ContextSerializer::class)
class NodeContextWrapper(impl: NodeContext) : NodeContext by impl

fun NodeContext.wrapped(): NodeContextWrapper =
    if (this is NodeContextWrapper) this else NodeContextWrapper(this)
