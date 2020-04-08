package raid.neuroide.reproto

internal class PersistentIdIssuer(private val site: String, private val persistentIdCounter: PersistentValue<Long>) {
    private var idCounter: Long = persistentIdCounter.get() ?: 0

    fun issueId(): String =
        synchronized(this) {
            idCounter++
            persistentIdCounter.set(idCounter)
            "${site}::${idCounter}"
        }
}
