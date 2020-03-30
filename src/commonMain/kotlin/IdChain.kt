package raid.neuroide.reproto

import kotlinx.serialization.Serializable

@Serializable
class IdChain constructor(val chain: List<String>) {
    private var index = 0

    constructor(vararg ids: String) : this(ids.toList())

    fun shift(): String = chain[index++]

    val hasNext
        get() = index < chain.size
}
