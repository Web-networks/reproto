package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable

@Serializable
data class Doublet(val index: Int, val site: String) : Comparable<Doublet> {
    override fun compareTo(other: Doublet) =
        compareValuesBy(this, other, Doublet::index, Doublet::site)
}