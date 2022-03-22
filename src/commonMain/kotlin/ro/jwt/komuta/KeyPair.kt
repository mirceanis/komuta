package ro.jwt.komuta

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

/**
 * Generates a new key pair
 */
fun Curve25519.keyPair(): KeyPair {
    val sk = randomBytes(SCALAR_BYTES)
    return keyPairFromScalar(sk)
}

/**
 * Generates a new key pair from an existing byte array.
 * At most the first 32 bytes are used from the input byte array; everything else is truncated.
 */
fun Curve25519.keyPairFromScalar(scalar: ByteArray): KeyPair {
    val sk = ByteArray(SCALAR_BYTES)
    scalar.copyInto(
        sk, 0, 0, min(SCALAR_BYTES, scalar.size)
    )
    sk[31] = (sk[31] and 127) or 64
    sk[0] = sk[0] and 248.toByte()
    return KeyPair(Scalar(sk), Point.fromPrivate(sk))
}

/**
 * Encapsulates a pair of corresponding keys, private and public.
 */
data class KeyPair(val privateKey: Scalar, val publicKey: Point)