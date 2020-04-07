package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId

class DefaultContext(site: String, idCounterInitial: Int = 0) : NodeContext {
    // TODO: use another id generation strategy for client
    // because backend can hardly know this counter to persist it between reloads
    // but we apparently may take the counter from the replicated log
    private var idCounter: Int = idCounterInitial

    override val siteId = LocalSiteId(site)

    override fun issueId(): String {
        idCounter++
        return "${siteId.id}::${idCounter}"
    }
}
