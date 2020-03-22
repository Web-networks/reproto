package raid.neuroide.reproto.crdt.seq

interface AllocationStrategy {
    fun allocatePosition(
        left: Array<out Doublet>,
        right: Array<out Doublet>,
        site: String
    ): Array<out Doublet>
}
