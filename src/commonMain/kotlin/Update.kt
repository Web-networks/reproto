package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.LamportTimestamp
import raid.neuroide.reproto.crdt.Operation

@Serializable
internal class Update(val id: IdChain, val index: LamportTimestamp, val payload: UpdatePayload)

@Serializable
internal class UpdatePayload(val operation: Operation)
