package raid.neuroide.reproto.crdt

import kotlinx.serialization.ContextSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import raid.neuroide.reproto.common.ContextualInjectorSerializer
import raid.neuroide.reproto.crdt.LWWRegister.LWWRegisterSet
import raid.neuroide.reproto.crdt.seq.AllocationStrategy
import raid.neuroide.reproto.crdt.seq.LogootStrategy
import raid.neuroide.reproto.crdt.seq.SequenceOperation


interface Operation

interface Upstream {
    fun deliver(op: Operation)
}

abstract class Crdt {
    @Transient
    protected var myUpstream: Upstream? = null

    fun setUpstream(upstream: Upstream) {
        this.myUpstream = upstream
    }

    abstract fun deliver(op: Operation)
}

@OptIn(kotlinx.serialization.ImplicitReflectionSerializer::class) // TODO: use module-wide opt-in
@Serializable(with = ContextSerializer::class)
data class LocalSiteId(val id: String)


fun getCrdtSerializers(siteId: LocalSiteId) =
    SerializersModule {
        contextual(LocalSiteId::class, ContextualInjectorSerializer(siteId))

        polymorphic(Operation::class) {
            LWWRegisterSet::class with LWWRegisterSet.serializer()
            SequenceOperation::class with SequenceOperation.serializer()
        }

        polymorphic(AllocationStrategy::class) {
            LogootStrategy::class with LogootStrategy.serializer()
        }
    }
