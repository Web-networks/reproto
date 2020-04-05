package raid.neuroide.reproto

interface PersistentValue<V> {
    fun get(): V?
    fun set(v: V)
}

class InMemoryValue<V> : PersistentValue<V> {
    private var value: V? = null

    override fun get(): V? = value

    override fun set(v: V) {
        value = v
    }
}