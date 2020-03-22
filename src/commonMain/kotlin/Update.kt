package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.Operation

@Serializable
class Update(val id: IdChain, val payload: UpdatePayload)

@Serializable
class UpdatePayload(val operation: Operation)
