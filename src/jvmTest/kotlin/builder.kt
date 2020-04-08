package raid.neuroide.reproto

import kotlinx.coroutines.runBlocking


class TestBuilder {
    private val fakeCtx = TestIdHelper("...")

    val service = ServiceNode("", 10)
    val bridge = Bridge(service)

    init {
        service.addGateway(MockStorage())
    }

    fun client() =
        ClientNode(fakeCtx.issueId()).apply {
            setGateway(bridge.newClientGateway())
        }
}

inline fun integrationTest(crossinline test: suspend TestBuilder.() -> Unit) = runBlocking {
    TestBuilder().test()
}
