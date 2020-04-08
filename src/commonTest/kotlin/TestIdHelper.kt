package raid.neuroide.reproto

import raid.neuroide.reproto.crdt.LocalSiteId

class TestIdHelper(site: String) {
    private var idCounter: Int = 0

    val siteId = LocalSiteId(site)

    fun issueId(): String {
        idCounter++
        return "${siteId.id}::${idCounter}"
    }
}
