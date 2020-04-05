package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId

internal class ServiceContext(private val site: String, private val persistentIdCounter: PersistentValue<Long>) :
    NodeContext {
    private var idCounter: Long = persistentIdCounter.get() ?: 0

    override val siteId = LocalSiteId(site)

    override fun issueId(): String =
        synchronized(this) {
            idCounter++
            persistentIdCounter.set(idCounter)
            "${site}::${idCounter}"
        }
}
