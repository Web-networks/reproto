package raid.neuroide.reproto.crdt.seq

import kotlinx.serialization.Serializable

@Serializable
data class SequenceElement(val pid: Identifier, val content: String) : Comparable<SequenceElement> {
    override fun compareTo(other: SequenceElement) =
        compareValuesBy(this, other, SequenceElement::pid, SequenceElement::content)
}
