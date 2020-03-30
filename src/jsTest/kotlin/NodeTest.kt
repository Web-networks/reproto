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
