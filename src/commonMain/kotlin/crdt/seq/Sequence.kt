package raid.neuroide.reproto.crdt.seq

import raid.neuroide.reproto.crdt.Crdt
import raid.neuroide.reproto.crdt.Operation
import raid.neuroide.reproto.crdt.PlainClock

private val LeftId = Identifier(emptyArray(), -1)
private val RightId = Identifier(arrayOf(Doublet(Int.MAX_VALUE, "")), -1) // TODO: fix


// TODO: implement and use SortedSet
class Sequence(private val site: String, private val strategy: AllocationStrategy) : Crdt() {
    private val clock = PlainClock()
    private val elements: MutableMap<Identifier, String> = mutableMapOf(LeftId to "", RightId to "")
    private var _sortedIdentifiers: List<Identifier>? = null

    private val sortedIdentifiers: List<Identifier>
        get() {
            if (_sortedIdentifiers == null)
                _sortedIdentifiers = elements.keys.sorted()
            return _sortedIdentifiers!!
        }

    val content: List<String>
        get() = sortedIdentifiers.mapNotNull {
            if (it == LeftId || it == RightId)
                null
            else
                elements[it]
        }

    val size: Int
        get() = elements.size - 2

    operator fun get(index: Int): String {
        return content[index]
    }

    fun insert(index: Int, content: String) {
        checkLimits(index, true)

        val lId = sortedIdentifiers[index]
        val rId = sortedIdentifiers[index + 1]

        val newId = allocateIdentifier(lId, rId)
        val op = SequenceOperationInsert(newId, content)
        commitLocallyGenerated(op)
    }

    fun delete(index: Int) {
        checkLimits(index)
        val id = sortedIdentifiers[index + 1]

        val op = SequenceOperationDelete(id)
        commitLocallyGenerated(op)
    }

    fun move(from: Int, to: Int) {
        checkLimits(from)
        checkLimits(to, true)

        val fromId = sortedIdentifiers[from + 1]
        val toLId = sortedIdentifiers[to]
        val toRId = sortedIdentifiers[to + 1]

        val newId = allocateIdentifier(toLId, toRId)
        val op = SequenceOperationMove(fromId, newId)
        commitLocallyGenerated(op)
    }

    private fun commitLocallyGenerated(op: SequenceOperation) {
        deliver(op)
        myUpstream?.deliver(op)
    }

    private fun checkLimits(index: Int, allowEnd: Boolean = false) {
        val rightLimit = if (allowEnd) size else size - 1
        if (index !in 0..rightLimit)
            throw IndexOutOfBoundsException()
    }

    private fun allocateIdentifier(left: Identifier, right: Identifier): Identifier {
        val position = strategy.allocatePosition(left.position, right.position, site)
        return Identifier(position, clock.next())
    }

    override fun deliver(op: Operation) {
        when (val operation = op as SequenceOperation) {
            is SequenceOperationInsert -> {
                elements[operation.pid] = operation.content
            }
            is SequenceOperationDelete -> {
                elements.remove(operation.pid)
            }
            is SequenceOperationMove -> {
                elements[operation.pidFrom]?.let {
                    elements[operation.pidTo] = it
                    elements.remove(operation.pidFrom)
                }
            }
            else -> return
        }
        _sortedIdentifiers = null
    }
}
