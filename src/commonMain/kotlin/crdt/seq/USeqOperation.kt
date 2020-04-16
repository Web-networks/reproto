package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.LamportTimestamp
import raid.neuroide.reproto.crdt.Operation

@Serializable
sealed class USeqOperation : Operation {
    @Serializable
    data class Emplace(val element: String, val pid: Identifier, val time: LamportTimestamp) : USeqOperation()

    @Serializable
    data class Delete(val element: String) : USeqOperation()
}
