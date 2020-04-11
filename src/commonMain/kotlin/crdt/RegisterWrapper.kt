package raid.neuroide.reproto.crdt

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


private abstract class TypedAlias<F, V>(
    private val sourceProperty: KMutableProperty0<F>
) {
    abstract fun serialize(v: V): F
    abstract fun deserialize(v: F): V

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return deserialize(sourceProperty.get())
    }

    @Suppress("UNCHECKED_CAST")
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        sourceProperty.set(serialize(value))
    }
}

private class IntAlias(sourceProperty: KMutableProperty0<String>) : TypedAlias<String, Int>(sourceProperty) {
    override fun serialize(v: Int) = v.toString()
    override fun deserialize(v: String) = if (v.isEmpty()) 0 else v.toInt()
}

private class LongAlias(sourceProperty: KMutableProperty0<String>) : TypedAlias<String, Long>(sourceProperty) {
    override fun serialize(v: Long) = v.toString()
    override fun deserialize(v: String) = if (v.isEmpty()) 0 else v.toLong()
}

private class BooleanAlias(sourceProperty: KMutableProperty0<String>) : TypedAlias<String, Boolean>(sourceProperty) {
    override fun serialize(v: Boolean) = (if (v) 1 else 0).toString()
    override fun deserialize(v: String) = if (v.isEmpty()) false else v.toInt() != 0
}

private class DoubleAlias(sourceProperty: KMutableProperty0<String>) : TypedAlias<String, Double>(sourceProperty) {
    override fun serialize(v: Double) = v.toRawBits().toString()
    override fun deserialize(v: String) = if (v.isEmpty()) .0 else Double.fromBits(v.toLong())
}


class RegisterWrapper(private val register: LWWRegister) {
    var value: String
        get() = register.value
        set(value) {
            register.value = value
        }

    var intValue by IntAlias(::value)
    var longValue by LongAlias(::value)
    var doubleValue by DoubleAlias(::value)
    var booleanValue by BooleanAlias(::value)
}
