package raid.neuroide.reproto.crdt

import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import raid.neuroide.reproto.crdt.LWWRegister.LWWRegisterSet


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


fun getCrdtSerializers() =
    SerializersModule {
        polymorphic(Operation::class) {
            LWWRegisterSet::class with LWWRegisterSet.serializer()
        }
    }
