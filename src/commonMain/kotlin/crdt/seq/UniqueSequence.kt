package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import raid.neuroide.reproto.common.IndexedSet
import raid.neuroide.reproto.common.indexedSetOf
import raid.neuroide.reproto.crdt.LamportTimestamp
import raid.neuroide.reproto.crdt.LocalSiteId
import raid.neuroide.reproto.crdt.ObservableCrdt
import raid.neuroide.reproto.crdt.Operation


@Serializable
class UniqueSequence(private val siteId: LocalSiteId, private val strategy: AllocationStrategy) :
    ObservableCrdt<Change>() {
    private val order: IndexedSet<Triplet> = indexedSetOf(Triplet(LeftId), Triplet(RightId))

    @Transient
    private val contents: MutableMap<String, Triplet> = mutableMapOf()

    init {
        order.filter { it.pid != LeftId && it.pid != RightId }.forEach {
            contents[it.element] = it
        }
    }

    val elements: List<String>
        get() = order.mapNotNull {
            if (it.pid == LeftId || it.pid == RightId)
                null
            else
                it.element
        }

    val size: Int
        get() = contents.size

    operator fun get(index: Int): String {
        if (index >= size)
            throw IndexOutOfBoundsException()
        return order[index + 1].element
    }

    operator fun contains(element: String) = element in contents

    fun add(index: Int): String {
        checkLimits(index, true)

        val lId = order[index].pid
        val rId = order[index + 1].pid
        val newId = allocateIdentifier(lId, rId)

        val localIndex = nextUpstreamIndex().toString()
        val element = "${siteId.id}::$localIndex"

        val op = USeqOperation.Emplace(element, newId, LamportTimestamp(0, siteId.id))
        commitLocallyGenerated(op)

        return element
    }

    fun delete(index: Int) {
        checkLimits(index)
        val element = order[index + 1].element
        deleteUnchecked(element)
    }

    private fun deleteUnchecked(element: String) {
        val op = USeqOperation.Delete(element)
        commitLocallyGenerated(op)
    }

    fun move(from: Int, to: Int) {
        checkLimits(from)
        checkLimits(to, true)

        val triplet = order[from + 1]
        moveUnchecked(triplet, to)
    }

    private fun moveUnchecked(triplet: Triplet, to: Int) {
        val toLId = order[to].pid
        val toRId = order[to + 1].pid
        val newId = allocateIdentifier(toLId, toRId)

        val newTime = LamportTimestamp(triplet.time.time + 1, siteId.id)
        val op = USeqOperation.Emplace(triplet.element, newId, newTime)
        commitLocallyGenerated(op)
    }

    private fun commitLocallyGenerated(op: USeqOperation) {
        deliver(op)
        myUpstream?.deliver(op)
    }

    private fun checkLimits(index: Int, allowEnd: Boolean = false) {
        val rightLimit = if (allowEnd) size else size - 1
        if (index !in 0..rightLimit)
            throw IndexOutOfBoundsException()
    }

    private fun allocateIdentifier(left: Identifier, right: Identifier): Identifier {
        val position = strategy.allocatePosition(left.position, right.position, siteId.id)
        return Identifier(position, nextUpstreamIndex())
    }

    private fun nextUpstreamIndex(): Long {
        val upstream = myUpstream ?: throw IllegalStateException("Upstream is required to generate identifier")
        return upstream.nextLocalIndex()
    }

    override fun deliver(op: Operation) {
        when (val operation = op as USeqOperation) {
            is USeqOperation.Emplace -> {
                val (element, pid, time) = operation
                val newTriplet = Triplet(element, pid, time)
                val isNew = (time.time == 0L)
                val triplet = contents[element]

                if (triplet == null && isNew) {
                    // insert
                    contents[element] = newTriplet
                    val index = order.addIndexed(newTriplet)

                    fire(Change.Insert(index - 1, element))
                } else if (triplet != null) {
                    // move
                    if (triplet.time < time) {
                        val fromIndex = order.indexOf(triplet)

                        order.removeAt(fromIndex)
                        contents[element] = newTriplet
                        val toIndex = order.addIndexed(newTriplet)

                        fire(Change.Move(fromIndex - 1, toIndex - 1, element))
                    }
                } // else triplet == null && !isNew -> element was deleted
            }
            is USeqOperation.Delete -> {
                val element = operation.element
                val triplet = contents[element] ?: return

                val index = order.removeIndexed(triplet)
                contents.remove(element)

                fire(Change.Delete(index - 1, triplet.element))
            }
        }
    }

    @Serializable
    private class Triplet(val element: String, val pid: Identifier, val time: LamportTimestamp) :
        Comparable<Triplet> {
        constructor(pid: Identifier) : this("", pid, LamportTimestamp(0, ""))

        override fun compareTo(other: Triplet): Int {
            return compareValues(pid, other.pid)
        }
    }
}
