package raid.neuroide.reproto

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class ServiceTest {
    @Test
    fun basic() {
        val s = service()
        assertNull(runBlocking { s.load("model") })

        val id = s.createPrototype()
        assertNotNull(runBlocking { s.load(id) })
    }

    @Test
    fun preempt() {
        val s = service(3)

        val id = s.createPrototype()
        repeat(5) { s.createPrototype() }

        assertNotNull(runBlocking { s.load(id) })
    }

    private fun service(threshold: Int = 10) =
        ServiceNode("", threshold).apply {
            addGateway(MockStorage())
        }
}
