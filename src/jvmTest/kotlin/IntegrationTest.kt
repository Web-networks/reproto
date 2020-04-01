package raid.neuroide.reproto

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IntegrationTest {
    @Test
    fun prototype() = integrationTest {
        val c = client()

        assertNull(c.getPrototypeSync("id"))

        val id = service.createPrototype()
        assertNotNull(c.getPrototypeSync(id))
    }

//    @Test
    fun modelStore() = integrationTest {
        val id = service.createPrototype()
        val p1 = client().getPrototypeSync(id)
        val p2 = client().getPrototypeSync(id)
        assertNotNull(p1)
        assertNotNull(p2)

        repeat(100) { service.createPrototype() }


        p1.addLayer(0)
        // TODO: wait for io completion
        assertEquals(1, p2.layers.size)

        repeat(100) { service.createPrototype() }

        val p3 = client().getPrototypeSync(id)
        assertNotNull(p3)

        assertEquals(1, p1.layers.size)
        assertEquals(1, p2.layers.size)
        assertEquals(1, p3.layers.size)

        p2.addLayer(0)
        assertEquals(2, p1.layers.size)
        assertEquals(2, p2.layers.size)
        assertEquals(2, p3.layers.size)
    }

    private fun ClientNode.getPrototypeSync(id: String): Prototype? {
        var p: Prototype? = null
        getPrototype(id) { p = it }
        return p
    }
}