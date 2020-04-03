package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.Operation

@Serializable
sealed class SequenceOperation : Operation

@Serializable
data class SequenceOperationInsert(val pid: Identifier, val content: String) : SequenceOperation()

@Serializable
data class SequenceOperationDelete(val pid: Identifier) : SequenceOperation()

@Serializable
data class SequenceOperationMove(val pidFrom: Identifier, val pidTo: Identifier) : SequenceOperation()
