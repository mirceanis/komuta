import ro.jwt.komuta.*

/**
 * Encapsulates all the data related to a message masked by multiple players using an ElGamal style encryption.
 * The assumption is that your message is already mapped onto a msgEmbedding.
 */
data class Komuta(
    /**
     * The combined public key of all the players participating in masking
     */
    val pk: Point = Point.ZERO,

    /**
     * The combined "ephemeral" public key of all the mask operations.
     */
    val epk: Point = Point.ZERO,

    /**
     * The resulting masked message.
     *
     * When the combined public key `pk` is [Point.ZERO], this is the cleartext message,
     * assuming that unmasking has been performed with the correct keys.
     */
    val msg: Point
) {

    /**
     * Applies an extra mask to the message.
     */
    fun mask(nonce: Scalar = Scalar.random()): Komuta {
        val c1 = Point.fromPrivate(nonce)
        val c2 = this.pk.scalarMult(nonce)
        return Komuta(this.pk, this.epk.add(c1), this.msg.add(c2))
    }

    /**
     * Remove one layer of masking.
     *
     * !!!WARNING!!!!
     * This method assumes that the secret key used here corresponds to one of the public keys used in the combined
     * public key. If a wrong key is used, then the result is unrecoverably CORRUPTED.
     */
    fun unmask(secretKey: Scalar): Komuta {
        val playerPublicKey = Point.fromPrivate(secretKey)
        val d1 = this.epk.scalarMult(secretKey)
        return Komuta(this.pk.sub(playerPublicKey), this.epk, this.msg.sub(d1))
    }

    fun isMasked(): Boolean {
        return this.pk.serialize().equals(Point.ZERO.serialize())
    }

    override fun toString(): String {
        return arrayOf(this.pk, this.epk, this.msg).map { it.serialize() }.joinToString(",")
    }

    companion object {
        fun fromString(input: String): Komuta {
            val components = input.split(",")
            if (components.size != 3) {
                throw IllegalArgumentException("The input is not formatted correctly.")
            }
            val (pk, epk, msg) = components.map { Point(it) }
            return Komuta(pk, epk, msg)
        }
    }
}