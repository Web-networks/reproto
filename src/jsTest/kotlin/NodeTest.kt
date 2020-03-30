package raid.neuroide.reproto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NodeTest {
    private val server = MockServer()

    @Test
    fun basicSingle() {
        val node = node()
        var ready = false
        node.getPrototype("test") {
            // mock server is essentially synchronous
            ready = it != null
        }
        assertTrue(ready)
    }

    @Test
    fun layersAddRemove() {
        val n1 = node()
        val n2 = node()
        val p1: Prototype? = n1.getPrototypeSync("proto")
        val p2: Prototype? = n2.getPrototypeSync("proto")

        assertNotNull(p1)
        assertNotNull(p2)

        assertEquals(0, p1.layers.size)

        p1.addLayer(0)

        assertEquals(1, p1.layers.size)
        assertEquals(1, p2.layers.size)

        p2.removeLayer(0)

        assertEquals(0, p1.layers.size)
        assertEquals(0, p2.layers.size)
    }

    @Test
    fun parameters() {
        val p1: Prototype? = node().getPrototypeSync("proto")
        val p2: Prototype? = node().getPrototypeSync("proto")
        assertNotNull(p1)
        assertNotNull(p2)

        val l1 = p1.addLayer(0)
        val l2 = p2.layers[0]

        l1["test"].intValue = 10
        assertEquals(10, l1["test"].intValue)
        assertEquals(10, l2["test"].intValue)

        l2["test"].intValue = 13
        assertEquals(13, l1["test"].intValue)
        assertEquals(13, l2["test"].intValue)
    }

    private fun node() =
        ClientNode(server.context.issueId()).apply {
            setGateway(server)
        }

    private fun ClientNode.getPrototypeSync(id: String): Prototype? {
        var p: Prototype? = null
        getPrototype(id) { p = it }
        return p
    }
}
