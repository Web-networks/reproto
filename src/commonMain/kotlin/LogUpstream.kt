package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.VectorTimestamp


interface LogUpstream {
    fun save(update: Update)
    fun restore(sinceRevision: VectorTimestamp, maxCount: Int): List<Update>?
}
