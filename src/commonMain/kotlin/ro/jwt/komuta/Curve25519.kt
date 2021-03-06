package ro.jwt.komuta

import kotlin.math.min

object Curve25519 {

    const val POINT_BYTES = 32
    private const val SCALAR_BYTES = 32

    /**
     * scalar multiplication
     */
    fun scalarMult(scalar: ByteArray, point: ByteArray): ByteArray {
        require(scalar.size == SCALAR_BYTES) { "bad scalar size, should be $SCALAR_BYTES bytes" }
        require(point.size == POINT_BYTES) { "bad point size, should be $POINT_BYTES bytes" }
        val q = ByteArray(POINT_BYTES)
        Curve25519LowLevel.scalarMult(q, scalar, point)
        return q
    }

    /**
     * point addition
     */
    fun add(p1: ByteArray, p2: ByteArray): ByteArray {
        require(p1.size == POINT_BYTES) { "bad point size, p1 should be $POINT_BYTES bytes" }
        require(p2.size == POINT_BYTES) { "bad point size, p2 should be $POINT_BYTES bytes" }

        val point1 = LongArray(16)
        val point2 = LongArray(16)
        val resultPoint = LongArray(16)

        Curve25519LowLevel.unpack25519(point1, p1)
        Curve25519LowLevel.unpack25519(point2, p2)
        Curve25519LowLevel.add(resultPoint, point1, point2)
        val q = ByteArray(POINT_BYTES)
        Curve25519LowLevel.pack25519(q, resultPoint)
        return q
    }

    /**
     * point subtraction
     */
    fun sub(p1: ByteArray, p2: ByteArray): ByteArray {
        require(p1.size == POINT_BYTES) { "bad point size, p1 should be $POINT_BYTES bytes" }
        require(p2.size == POINT_BYTES) { "bad point size, p2 should be $POINT_BYTES bytes" }

        val point1 = LongArray(16)
        val point2 = LongArray(16)
        val resultPoint = LongArray(16)

        Curve25519LowLevel.unpack25519(point1, p1)
        Curve25519LowLevel.unpack25519(point2, p2)
        Curve25519LowLevel.subtract(resultPoint, point1, point2)
        val q = ByteArray(POINT_BYTES)
        Curve25519LowLevel.pack25519(q, resultPoint)
        return q
    }

    /**
     * Generates a new key pair
     */
    fun keyPair(): KeyPair {
        val sk = randomBytes(SCALAR_BYTES)
        val pk = ByteArray(POINT_BYTES)
        Curve25519LowLevel.scalarMultBase(pk, sk)
        return KeyPair(sk, PublicKey(pk.encodeBase64()))
    }

    /**
     * Generates a new key pair from an existing byte array.
     * At most 32 bytes are used from the input byte array.
     */
    fun keyPairFromScalar(scalar: ByteArray): KeyPair {
        val sk = ByteArray(SCALAR_BYTES)
        scalar.copyInto(
            sk, 0, 0, min(SCALAR_BYTES, scalar.size)
        )
        val pk = ByteArray(POINT_BYTES)
        Curve25519LowLevel.scalarMultBase(pk, sk)
        return KeyPair(sk, PublicKey(pk.encodeBase64()))
    }

    class KeyPair(val privateKey: ByteArray, val publicKey: PublicKey)
}

