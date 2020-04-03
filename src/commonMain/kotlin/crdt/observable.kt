package raid.neuroide.reproto.crdt

interface Observable<Change> {
    fun addListener(l: (Change) -> Unit)
    fun fire(change: Change)
}

class ObservableData<Change> : Observable<Change> {
    private val listeners: MutableList<(Change) -> Unit> = mutableListOf()

    override fun addListener(l: (Change) -> Unit) {
        listeners.add(l)
    }
    
    override fun fire(change: Change) {
        for (l in listeners) {
            l(change)
        }
    }
}