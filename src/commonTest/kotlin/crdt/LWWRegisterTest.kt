package raid.neuroide.reproto.crdt

import kotlin.test.Test
import kotlin.test.assertEquals

class LWWRegisterTest {
    private companion object {
        const val v1 = ""
        const val v2 = "kek"
        const val v3 = "kuk"
    }

    @Test
    fun basic() = crdtTest {
        val a = lwwRegister(v2)
        val b = lwwRegister(v1)

        assertEquals(v2, a.value)
        assertEquals(v1, b.value)

        a.value = v2
        assertEquals(v2, a.value)
        assertEquals(v2, b.value)

        b.value = v3
        assertEquals(v3, a.value)
        assertEquals(v3, b.value)
    }

    @Test
    fun concurrent() = crdtTest {
        val a = lwwRegister()
        val b = lwwRegister()

        disconnect()

        b.value = v3
        a.value = v2

        assertEquals(v2, a.value)
        assertEquals(v3, b.value)

        connect()

        assertEquals(v3, a.value)
        assertEquals(v3, b.value)
    }
}
