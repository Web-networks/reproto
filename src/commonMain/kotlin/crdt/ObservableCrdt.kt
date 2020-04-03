package raid.neuroide.reproto.crdt

abstract class ObservableCrdt<Change> : Crdt() {
    private val listeners: MutableList<(Change) -> Unit> = mutableListOf()

    fun addListener(l: (Change) -> Unit) {
        listeners.add(l)
    }
    
    protected fun fire(change: Change) {
        for (l in listeners) {
            l(change)
        }
    }
}
