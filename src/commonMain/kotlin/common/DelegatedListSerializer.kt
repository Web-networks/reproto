package raid.neuroide.reproto.common

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list

abstract class DelegatedListSerializer<T, E>(elementSerializer: KSerializer<E>) : KSerializer<T> {
    private val delegate = elementSerializer.list

    override val descriptor: SerialDescriptor = delegate.descriptor

    protected abstract fun extract(value: T): List<E>
    protected abstract fun construct(list: List<E>): T

    override fun deserialize(decoder: Decoder): T {
        return construct(delegate.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: T) {
        return delegate.serialize(encoder, extract(value))
    }
}

abstract class DelegatedMapSerializer<T, K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
) : KSerializer<T> {
    private val delegate = MapSerializer(keySerializer, valueSerializer)

    override val descriptor: SerialDescriptor = delegate.descriptor

    protected abstract fun extract(value: T): Map<K, V>
    protected abstract fun construct(map: Map<K, V>): T

    override fun deserialize(decoder: Decoder): T {
        return construct(delegate.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: T) {
        return delegate.serialize(encoder, extract(value))
    }
}
