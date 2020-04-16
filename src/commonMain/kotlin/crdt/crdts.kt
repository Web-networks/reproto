package raid.neuroide.reproto.crdt

import kotlinx.serialization.ContextSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import raid.neuroide.reproto.common.ContextualInjectorSerializer
import raid.neuroide.reproto.crdt.LWWRegister.LWWRegisterSet
import raid.neuroide.reproto.crdt.seq.AllocationStrategy
import raid.neuroide.reproto.crdt.seq.LogootStrategy
import raid.neuroide.reproto.crdt.seq.SequenceOperation
import raid.neuroide.reproto.crdt.seq.USeqOperation


interface Operation

interface Upstream {
    fun deliver(op: Operation)
    fun nextLocalIndex(): Long
}

abstract class Crdt {
    protected var myUpstream: Upstream? = null

    fun setUpstream(upstream: Upstream) {
        this.myUpstream = upstream
    }

    abstract fun deliver(op: Operation)
}


@Serializable(with = ContextSerializer::class)
data class LocalSiteId(val id: String)


fun getCrdtSerializers(siteId: LocalSiteId): SerialModule {
    return SerializersModule {
        contextual(LocalSiteId::class, ContextualInjectorSerializer(siteId))

        polymorphic(Operation::class) {
            LWWRegisterSet::class with LWWRegisterSet.serializer()

            SequenceOperation.Insert::class with SequenceOperation.Insert.serializer()
            SequenceOperation.Delete::class with SequenceOperation.Delete.serializer()

            USeqOperation.Emplace::class with USeqOperation.Emplace.serializer()
            USeqOperation.Delete::class with USeqOperation.Delete.serializer()
        }

        polymorphic(AllocationStrategy::class) {
            LogootStrategy::class with LogootStrategy.serializer()
        }
    }
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
private inline fun <T> KSerializer<*>.cast(): KSerializer<T> = this as KSerializer<T>