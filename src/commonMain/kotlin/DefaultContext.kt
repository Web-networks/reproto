package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId

class DefaultContext(site: String) : NodeContext {
    private var idCounter: Int = 0

    override val siteId = LocalSiteId(site)

    override fun issueId(): String {
        idCounter++
        return "${siteId.id}::${idCounter}"
    }
}