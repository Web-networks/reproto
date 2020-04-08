package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import raid.neuroide.reproto.common.DelegatedListSerializer

@Serializable(with = IdChain.Serializer::class)
internal class IdChain constructor(val chain: List<String>) {
    @Transient
    private var index = 0

    constructor(vararg ids: String) : this(ids.toList())

    fun shift(): String = chain[index++]

    val hasNext
        get() = index < chain.size

    operator fun plus(id: String) =
        IdChain(chain + listOf(id))

    class Serializer : DelegatedListSerializer<IdChain, String>(String.serializer()) {
        override fun extract(value: IdChain): List<String> {
            return value.chain
        }

        override fun construct(list: List<String>): IdChain {
            return IdChain(list)
        }
    }
}
