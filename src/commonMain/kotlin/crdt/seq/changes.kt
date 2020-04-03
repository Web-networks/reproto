package raid.neuroide.reproto.crdt.seq

sealed class Change

data class InsertChange(val position: Int, val value: String) : Change()
data class DeleteChange(val position: Int, val value: String) : Change()
data class MoveChange(val from: Int, val to: Int, val value: String) : Change()
