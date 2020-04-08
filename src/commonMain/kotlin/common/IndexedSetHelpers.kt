package raid.neuroide.reproto.common

fun <K : Comparable<K>> indexedSetOf(vararg elements: K): IndexedSet<K> {
    return IndexedSet(elements.asList())
}
