package raid.neuroide.reproto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class IdChain constructor(val chain: List<String>) {
    @Transient
    private var index = 0

    constructor(vararg ids: String) : this(ids.toList())

    fun shift(): String = chain[index++]

    val hasNext
        get() = index < chain.size

    operator fun plus(id: String) =
        IdChain(chain + listOf(id))
}
