package raid.neuroide.reproto.crdt

class InternalBroadcast : Upstream {
    private val crdts: MutableList<Crdt> = mutableListOf()
    private val pending: MutableList<Operation> = mutableListOf()

    var connected: Boolean = true
        get() = field
        set(value) {
            if (value == field)
                return
            field = value
            if (value)
                deliverPendingOperations()
        }

    override fun deliver(op: Operation) {
        if (connected) {
            deliverOperation(op)
        } else {
            pending.add(op)
        }
    }

    fun addCrdt(crdt: Crdt) {
        crdts.add(crdt)
        crdt.setUpstream(this)
    }

    private fun deliverPendingOperations() {
        for (op in pending) {
            deliverOperation(op)
        }
        pending.clear()
    }

    private fun deliverOperation(op: Operation) {
        for (c in crdts) {
            c.deliver(op)
        }
    }
}
