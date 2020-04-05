package raid.neuroide.reproto.crdt

abstract class ObservableCrdt<Change> : Crdt() {
    private var listener: ((Change) -> Unit)? = null

    fun setListener(l: ((Change) -> Unit)?) {
        listener = l
    }
    
    protected fun fire(change: Change) {
        listener?.invoke(change)
    }
}
