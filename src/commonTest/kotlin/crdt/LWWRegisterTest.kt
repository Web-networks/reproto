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

    @Test
    fun listener() = crdtTest {
        val a = lwwRegister()
        val b = lwwRegister()

        val alChecker = a.listen()
        val blChecker = b.listen()

        alChecker.assertNotCalled()
        blChecker.assertNotCalled()

        a.value = "Test"

        alChecker.assertOnce()
        blChecker.assertOnce()
    }

    @Test
    fun wrapper() = crdtTest {
        val iValue = 12
        val fValue = -12.3782
        val bValue = true
        val lValue = -(Long.MAX_VALUE / 2)

        val a = RegisterWrapper(lwwRegister())

        assertEquals(0, a.intValue)
        assertEquals(.0, a.doubleValue)
        assertEquals(0, a.longValue)
        assertEquals(false, a.booleanValue)

        a.intValue = iValue
        assertEquals(iValue, a.intValue)

        a.longValue = lValue
        assertEquals(lValue, a.longValue)

        a.doubleValue = fValue
        assertEquals(fValue, a.doubleValue)

        a.booleanValue = bValue
        assertEquals(bValue, a.booleanValue)
    }
}
