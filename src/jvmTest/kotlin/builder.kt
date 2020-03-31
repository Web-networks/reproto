package raid.neuroide.reproto


class TestBuilder {
    private val fakeCtx = DefaultContext("...")

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

inline fun integrationTest(test: TestBuilder.() -> Unit) {
    TestBuilder().test()
}
