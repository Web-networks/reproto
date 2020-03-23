package raid.neuroide.reproto

import kotlinx.serialization.Transient

abstract class ContextReceiver {
    @Transient
    protected lateinit var myContext: NodeContext

    open fun setContext(context: NodeContext) {
        myContext = context
    }
}
