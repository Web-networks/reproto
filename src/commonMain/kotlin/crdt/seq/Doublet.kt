package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable

@Serializable
data class Doublet(val index: Int, val site: String) : Comparable<Doublet> {
    // Int index is used rather than Long for performance reasons
    // because of the lack of native js Long implementation

    override fun compareTo(other: Doublet) =
        compareValuesBy(this, other, Doublet::index, Doublet::site)
}
