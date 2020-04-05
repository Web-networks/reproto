package raid.neuroide.reproto.crdt.seq

sealed class Change {
    data class Insert(val position: Int, val value: String) : Change()
    data class Delete(val position: Int, val value: String) : Change()
    data class Move(val from: Int, val to: Int, val value: String) : Change()
}