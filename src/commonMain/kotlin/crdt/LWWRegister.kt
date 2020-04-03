package raid.neuroide.reproto.crdt

import kotlinx.serialization.Serializable

@Serializable
class LWWRegister(private var payload: String, private val clock: LamportClock) : ObservableCrdt<Unit>() {
    private var time = clock.next()

    constructor(payload: String, siteId: LocalSiteId) : this(payload, LamportClock(siteId))

    var value: String
        get() = payload
        set(newValue) {
            time = clock.next()
            payload = newValue
            fire(Unit)
            sendUpstream()
        }

    private fun sendUpstream() {
        myUpstream?.deliver(LWWRegisterSet(payload, time))
    }

    override fun deliver(op: Operation) {
        val operation = op as? LWWRegisterSet ?: return
        if (operation.time <= time)
            return

        payload = operation.value
        time = operation.time
        clock.update(operation.time)
        fire(Unit)
    }

    @Serializable
    data class LWWRegisterSet(val value: String, val time: LamportTimestamp) : Operation
}
