package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.Operation

@Serializable
sealed class SequenceOperation : Operation {
    @Serializable
    data class Insert(val pid: Identifier, val content: String) : SequenceOperation()

    @Serializable
    data class Delete(val pid: Identifier) : SequenceOperation()
}
