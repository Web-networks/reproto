package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import raid.neuroide.reproto.crdt.Operation

@Serializable
sealed class SequenceOperation : Operation

@Serializable
class SequenceOperationInsert(val pid: Identifier, val content: String) : SequenceOperation()

@Serializable
class SequenceOperationDelete(val pid: Identifier) : SequenceOperation()

@Serializable
class SequenceOperationMove(val pidFrom: Identifier, val pidTo: Identifier) : SequenceOperation()
