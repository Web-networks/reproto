package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.Operation

@Serializable
internal class Update(val id: IdChain, val payload: UpdatePayload)

@Serializable
internal class UpdatePayload(val operation: Operation)
