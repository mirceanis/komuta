package ro.jwt.komuta

import kotlin.jvm.JvmInline

/**
 * Scalar element type.
 *
 * In our case, a private key, or nonce.
 */
@JvmInline
value class Scalar(internal val raw: ByteArray) {

    constructor(encodedRaw: String) : this(BaseCodec(BaseCodec.Base.base64url).decode(encodedRaw))

    fun serialize(): String {
        return BaseCodec(BaseCodec.Base.base64url).encode(this.raw)
    }

    companion object {
        fun random(): Scalar {
            return Curve25519.keyPair().privateKey
        }
    }
}