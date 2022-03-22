package ro.jwt.komuta

import org.junit.Assert
import org.junit.Test

class GroupTest {

    @Test
    fun `adding Point_ZERO results in the same point`() {
        val p = Point.random()
        val q = p.add(Point.ZERO)
        Assert.assertEquals(p.serialize(), q.serialize())
    }

    @Test
    fun `multiply zero`() {
        val q = Point.ZERO.scalarMult(Scalar.random())
        Assert.assertEquals(Point.ZERO.serialize(), q.serialize())
    }

}