package raid.neuroide.reproto.crdt.seq

interface AllocationStrategy {
    fun allocatePosition(
        left: List<Doublet>,
        right: List<Doublet>,
        site: String
    ): List<Doublet>
}
