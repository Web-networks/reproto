package raid.neuroide.reproto.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable(with = IndexedSet.Serializer::class)
class IndexedSet<K : Comparable<K>>() : List<K> {
    private val elements: MutableList<K> = mutableListOf()

    constructor(elements: List<K>) : this() {
        this.elements.addAll(elements)
        this.elements.sort()
    }

    override val size: Int
        get() = elements.size

    override fun iterator(): MutableIterator<K> {
        return elements.iterator()
    }

    override fun contains(element: K): Boolean {
        return elements.binarySearch(element) >= 0
    }

    fun addIndexed(element: K): Int {
        val position = elements.binarySearch(element)
        if (position >= 0)
            return position
        val insertionPoint = -position - 1;
        elements.add(insertionPoint, element)
        return insertionPoint
    }

    fun add(element: K): Boolean {
        val position = elements.binarySearch(element)
        if (position >= 0)
            return false
        elements.add(-position - 1, element)
        return true
    }

    override operator fun get(index: Int): K {
        return elements[index]
    }

    fun removeAt(index: Int): K {
        return elements.removeAt(index)
    }

    fun removeIndexed(element: K): Int {
        val index = indexOf(element)
        if (index >= 0)
            removeAt(index)
        return index
    }

    override fun indexOf(element: K): Int {
        return max(elements.binarySearch(element), -1)
    }

    override fun lastIndexOf(element: K): Int {
        return indexOf(element)
    }

    override fun listIterator(): ListIterator<K> {
        return elements.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<K> {
        return elements.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<K> {
        // not meaningful operation
        throw UnsupportedOperationException()
    }

    override fun containsAll(elements: Collection<K>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean = size == 0

    class Serializer<K : Comparable<K>>(elementSerializer: KSerializer<K>) :
        DelegatedListSerializer<IndexedSet<K>, K>(elementSerializer) {
        override fun extract(value: IndexedSet<K>): List<K> {
            return value.elements
        }

        override fun construct(list: List<K>): IndexedSet<K> {
            return IndexedSet(list)
        }
    }
}
