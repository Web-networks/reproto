package raid.neuroide.reproto

import kotlinx.serialization.Transient

abstract class ContextReceiver {
    @Transient
    protected lateinit var context: NodeContext

    open fun setContext(context: NodeContext) {
        this.context = context
    }
}
