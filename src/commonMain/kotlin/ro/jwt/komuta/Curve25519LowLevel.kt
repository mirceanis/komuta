@file:Suppress("ObjectPropertyName", "FunctionName")

package ro.jwt.komuta

import kotlin.experimental.and
import kotlin.experimental.or

/**
 * This is a port of Curve25519 from the the TweetNaCl library
 * Ported from the original C version by Mircea Nistor
 *
 * **DISCLAIMER:
 * This port has not gone through an audit.
 * Use at your own risk.**
 */
@Suppress("unused")
internal object Curve25519LowLevel {

    //base
    private val _9: ByteArray = ByteArray(32).apply { this[0] = 9 }

    private val _121665: LongArray = longArrayOf(0xDB41, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    /**
     * add carry subtract for all limbs of GF (normalize point after multiplication)
     */
    private fun car25519(o: LongArray) {
        for (i in 0 until 16) {
            o[i] += (1L shl 16)
            val c = o[i] shr 16
            o[(i + 1) * (if (i < 15) 1 else 0)] += c - 1 + 37 * (c - 1) * (if (i == 15) 1 else 0).toLong()
            o[i] -= c shl 16
        }
    }

    /**
     * Constant time swap of points `p`, `q` (GF).
     * Parameter `b` dictates whether or not a swap is actually performed.
     * b == 1 => swap
     * b == 0 => no swap
     */
    private fun sel25519(p: LongArray, q: LongArray, b: Int) {
        var t: Long
        val c = (b - 1).inv().toLong()
        for (i in 0 until 16) {
            t = c and (p[i] xor q[i])
            p[i] = p[i] xor t
            q[i] = q[i] xor t
        }
    }

    /**
     * Constant time pack a GF representation into a unique ByteArray, while making sure to fit the field
     * (always subtracts twice and then constant time swaps based on underflow)
     */
    internal fun pack25519(output: ByteArray, input: LongArray) {
        var b: Int
        val m = LongArray(16)
        val t = LongArray(16)
        for (i in 0 until 16) t[i] = input[i]
        car25519(t)
        car25519(t)
        car25519(t)
        for (j in 0 until 2) {
            m[0] = t[0] - 0xffed
            for (i in 1 until 15) {
                m[i] = t[i] - 0xffff - ((m[i - 1] shr 16) and 1)
                m[i - 1] = m[i - 1] and 0xffff
            }
            m[15] = t[15] - 0x7fff - ((m[14] shr 16) and 1)
            b = ((m[15] shr 16) and 1).toInt()
            m[14] = m[14] and 0xffff
            sel25519(t, m, 1 - b)
        }
        for (i in 0 until 16) {
            output[2 * i] = t[i].toByte()
            output[2 * i + 1] = (t[i] shr 8).toByte()
        }
    }

    /**
     * unpack a bytearray representation of a point into a GF
     */
    internal fun unpack25519(output: LongArray, input: ByteArray) {
        for (i in 0 until 16) {
            output[i] = (0xff and input[2 * i].toInt()) + (0xffL and input[2 * i + 1].toLong() shl 8)
        }
        output[15] = output[15] and 0x7fff
    }

    /**
     * add GF `a` + `b` and put result in `o`
     */
    internal fun add(o: LongArray, a: LongArray, b: LongArray) {
        for (i in 0 until 16) o[i] = a[i] + b[i]
    }

    /**
     * subtract GF `a` - `b` and put result in `o`
     */
    internal fun subtract(o: LongArray, a: LongArray, b: LongArray) {
        for (i in 0 until 16) o[i] = a[i] - b[i]
    }

    /**
     * multiply GF `a` * `b` and put result in `o`
     */
    private fun multiply(o: LongArray, a: LongArray, b: LongArray) {
        val t = LongArray(31)

        for (i in 0 until 16) {
            for (j in 0 until 16) {
                t[i + j] += a[i] * b[j]
            }
        }
        for (i in 0 until 15) {
            t[i] += 38 * t[i + 16]
        }
        for (i in 0 until 16) {
            o[i] = t[i]
        }
        car25519(o)
        car25519(o)
    }

    private fun square(o: LongArray, a: LongArray) = multiply(o, a, a)

    /**
     * modular inverse (division)
     */
    private fun inv25519(o: LongArray, i: LongArray) {
        val c = LongArray(16)
        for (a in 0 until 16) c[a] = i[a]
        for (a in 253 downTo 0) {
            square(c, c)
            if (a != 2 && a != 4) multiply(c, c, i)
        }
        for (a in 0 until 16) o[a] = c[a]
    }

    fun scalarMult(output: ByteArray, scalar: ByteArray, point: ByteArray): Int {
        val z = ByteArray(32)
        val x = LongArray(80)
        var r: Int
        val a = LongArray(16)
        val b = LongArray(16)
        val c = LongArray(16)
        val d = LongArray(16)
        val e = LongArray(16)
        val f = LongArray(16)
        for (i in 0 until 31) {
            z[i] = scalar[i]
        }
        z[31] = (scalar[31] and 127) or 64
        z[0] = z[0] and 248.toByte()
        unpack25519(x, point)
        for (i in 0 until 16) {
            b[i] = x[i]
            d[i] = 0
            a[i] = 0
            c[i] = 0
        }
        a[0] = 1
        d[0] = 1
        for (i in 254 downTo 0) {
            r = ((z[i shr 3].toLong() shr (i and 7)) and 1).toInt()
            sel25519(a, b, r)
            sel25519(c, d, r)
            add(e, a, c)
            subtract(a, a, c)
            add(c, b, d)
            subtract(b, b, d)
            square(d, e)
            square(f, a)
            multiply(a, c, a)
            multiply(c, b, e)
            add(e, a, c)
            subtract(a, a, c)
            square(b, a)
            subtract(c, d, f)
            multiply(a, c, _121665)
            add(a, a, d)
            multiply(c, c, a)
            multiply(a, d, f)
            multiply(d, b, x)
            square(b, e)
            sel25519(a, b, r)
            sel25519(c, d, r)
        }
        for (i in 0 until 16) {
            x[i + 16] = a[i]
            x[i + 32] = c[i]
            x[i + 48] = b[i]
            x[i + 64] = d[i]
        }

        val x32 = x.copyOfRange(32, x.size)
        val x16 = x.copyOfRange(16, x.size)
        inv25519(x32, x32)
        multiply(x16, x16, x32)
        pack25519(output, x16)

        return 0
    }

    fun scalarMultBase(q: ByteArray, n: ByteArray): Int {
        return scalarMult(q, n, _9)
    }

}
