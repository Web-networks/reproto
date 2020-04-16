package raid.neuroide.reproto.crdt

import raid.neuroide.reproto.crdt.seq.Change
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class USeqTest {
    @Test
    fun basicAdd() = crdtTest {
        val a = useq()
        val b = useq()

        assertEquals("", a.string)
        assertEquals("", b.string)

        val e0 = a.add(0)
        val e1 = b.add(0)
        assertNotEquals(e0, e1)
        val expected1 = e1 + e0

        assertEquals(expected1, a.string)
        assertEquals(expected1, b.string)

        val e2 = b.add(2)
        val e3 = a.add(1)
        val expected2 = e1 + e3 + e0 + e2

        assertEquals(expected2, a.string)
        assertEquals(expected2, b.string)
    }

    @Test
    fun concurrentInsert() = crdtTest {
        val a = useq()
        val b = useq()

        disconnect()

        val e0 = a.add(0)
        val e1 = b.add(0)

        assertEquals(e0, a.string)
        assertEquals(e1, b.string)

        connect()

        assertEquals(setOf(e0, e1), a.elements.toSet())
        assertEquals(a.elements, b.elements)

        disconnect()
        val s = a.elements

        val e2 = b.add(2)
        val e3 = a.add(1)

        connect()

        val expect = "${s[0]}$e3${s[1]}$e2"
        assertEquals(expect, a.string)
        assertEquals(expect, b.string)
    }

    @Test
    fun basicDelete() = crdtTest {
        val a = useq()
        val b = useq()

        val e0 = a.add(0)
        val e1 = a.add(1)

        b.delete(0)

        assertEquals(e1, a.string)
        assertEquals(e1, b.string)

        a.delete(0)

        assertEquals(0, a.elements.size)
        assertEquals(0, b.elements.size)
    }

    @Test
    fun basicMove() = crdtTest {
        val a = useq()
        val b = useq()

        val e0 = a.add(0)
        val e1 = a.add(0)

        b.move(0, 2)

        assertEquals(e0 + e1, a.string)

        b.move(1, 0)

        assertEquals(e1 + e0, a.string)
    }

    @Test
    fun concurrentMove() = crdtTest {
        val a = useq()
        val b = useq()

        val e0 = a.add(0)
        val e1 = a.add(1)
        val e2 = a.add(2)
        val set = setOf(e0, e1, e2)

        disconnect()

        a.move(1, 0)
        b.move(1, 3)

        connect()

        assertEquals(set, a.elements.toSet())
        assertEquals(a.elements, b.elements)
    }

    @Test
    fun listeners() = crdtTest {
        val a = useq()
        val b = useq()

        val al = a.listen()
        val bl = b.listen()
        val ls = arrayOf(al, bl)

        val e0 = a.add(0)
        for (l in ls)
            l.assertChange {
                assertTrue(it is Change.Insert)
                assertEquals(0, it.position)
                assertEquals(e0, it.value)
            }.assertOnce()

        val e1 = a.add(1)
        for (l in ls)
            l.reset()

        a.move(0, 2)
        for (l in ls)
            l.assertChange {
                assertTrue(it is Change.Move)
                assertEquals(0, it.from)
                assertEquals(1, it.to)
                assertEquals(e0, it.value)
            }.assertOnce().reset()

        b.delete(0)
        for (l in ls)
            l.assertChange {
                assertTrue(it is Change.Delete)
                assertEquals(0, it.position)
                assertEquals(e1, it.value)
            }.assertOnce().reset()
    }

    @Test
    fun exceptions() = crdtTest {
        val a = useq()

        assertFailsWith(IndexOutOfBoundsException::class) {
            a.add(1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            a.add(-1)
        }

        a.add(0)

        assertFailsWith(IndexOutOfBoundsException::class) {
            a.delete(1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            a.move(0, 2)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            a.move(1, 0)
        }
    }
}
