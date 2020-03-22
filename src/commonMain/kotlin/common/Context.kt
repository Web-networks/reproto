package raid.neuroide.reproto.common

class Context(private val data: Map<Key<*>, Any>) {
     constructor() : this(mapOf())
     constructor(vararg values: Pair<Key<*>, Any>) : this(mapOf(*values))

    @Suppress("UNCHECKED_CAST")
    operator fun <T>get(key: Key<T>): T? {
        return data[key] as T?
    }

    operator fun plus(other: Context): Context {
        val t: MutableMap<Key<*>, Any> = mutableMapOf()
        t.putAll(data)
        t.putAll(other.data)
        return Context(t)
    }

    interface Key<T>
}
