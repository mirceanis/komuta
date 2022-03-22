package ro.jwt.komuta

import kotlin.jvm.JvmInline

/**
 * Field element type.
 *
 * In our case, a point on the curve.
 *
 */
@JvmInline
value class Point(private val raw: ByteArray) {

    constructor(encodedRaw: String) : this(BaseCodec(BaseCodec.Base.base64url).decode(encodedRaw))

    fun add(other: Point): Point {
        return Point(Curve25519.add(this.raw, other.raw))
    }

    fun sub(other: Point): Point {
        return Point(Curve25519.sub(this.raw, other.raw))
    }

    fun scalarMult(scalar: Scalar): Point {
        return Point(Curve25519.scalarMult(scalar.raw, this.raw))
    }

    fun serialize(): String {
        return BaseCodec(BaseCodec.Base.base64url).encode(this.raw)
    }

    companion object {

        fun fromPrivate(privateRaw: Scalar): Point = BASE.scalarMult(privateRaw)
        fun fromPrivate(privateRaw: ByteArray): Point = fromPrivate(Scalar(privateRaw))

        fun random(): Point {
            return Point(Curve25519.keyPair().publicKey.raw)
        }

        val ZERO: Point = Point("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        val BASE: Point = Point("CQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
    }
}