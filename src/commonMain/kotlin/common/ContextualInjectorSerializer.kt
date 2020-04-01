package raid.neuroide.reproto.common

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind

internal class ContextualInjectorSerializer<T>(private val localValue: T) : KSerializer<T> {
    override val descriptor = SerialDescriptor("ValueInjection", StructureKind.OBJECT)

    override fun deserialize(decoder: Decoder): T {
        decoder.beginStructure(descriptor).endStructure(descriptor)
        return localValue
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.beginStructure(descriptor).endStructure(descriptor)
    }
}
