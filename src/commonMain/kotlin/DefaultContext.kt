package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId

class DefaultContext(site: String, idCounterInitial: Int = 0) : NodeContext {
    private var idCounter: Int = idCounterInitial

    override val siteId = LocalSiteId(site)

    override fun issueId(): String {
        idCounter++
        return "${siteId.id}::${idCounter}"
    }
}
