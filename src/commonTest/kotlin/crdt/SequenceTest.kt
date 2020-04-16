package raid.neuroide.reproto.crdt

import raid.neuroide.reproto.crdt.seq.Change
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SequenceTest {
    @Test
    fun basicInsert() = crdtTest {
        val a = logoot()
        val b = logoot()

        assertEquals("", a.string)
        assertEquals("", b.string)

        a.insert(0, "a")
        b.insert(0, "b")

        assertEquals("ba", a.string)
        assertEquals("ba", b.string)

        b.insert(2, "3")
        a.insert(1, "2")

        assertEquals("b2a3", a.string)
        assertEquals("b2a3", b.string)
    }

    @Test
    fun concurrentInsert() = crdtTest {
        val a = logoot()
        val b = logoot()

        disconnect()

        a.insert(0, "a")
        b.insert(0, "b")

        assertEquals("a", a.string)
        assertEquals("b", b.string)

        connect()

        assertEquals("ab", a.content.sorted().joinToString(""))
        assertEquals(a.string, b.string)

        disconnect()
        val s = a.string

        b.insert(2, "3")
        a.insert(1, "2")

        connect()

        val expect = "${s[0]}2${s[1]}3"
        assertEquals(expect, a.string)
        assertEquals(expect, b.string)
    }

    @Test
    fun basicDelete() = crdtTest {
        val a = logoot()
        val b = logoot()

        a.insert(0, "a")
        a.insert(1, "b")

        b.delete(0)

        assertEquals("b", a.string)
        assertEquals("b", b.string)

        a.delete(0)

        assertEquals("", a.string)
        assertEquals("", b.string)
    }

    @Test
    fun listeners() = crdtTest {
        val a = logoot()
        val b = logoot()

        val al = a.listen()
        val bl = b.listen()
        val ls = arrayOf(al, bl)

        a.insert(0, "x")
        for (l in ls)
            l.assertChange {
                assertTrue(it is Change.Insert)
                assertEquals(0, it.position)
                assertEquals("x", it.value)
            }.assertOnce()

        a.insert(1, "y")
        for (l in ls)
            l.reset()

        b.delete(0)
        for (l in ls)
            l.assertChange {
                assertTrue(it is Change.Delete)
                assertEquals(0, it.position)
                assertEquals("x", it.value)
            }.assertOnce().reset()
    }

    @Test
    fun exceptions() = crdtTest {
        val a = logoot()

        assertFailsWith(IndexOutOfBoundsException::class) {
            a.insert(1, "x")
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            a.insert(-1, "x")
        }

        a.insert(0, "x")

        assertFailsWith(IndexOutOfBoundsException::class) {
            a.delete(1)
        }
    }
}
