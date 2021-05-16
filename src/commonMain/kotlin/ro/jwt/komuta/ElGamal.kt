package ro.jwt.komuta

import ro.jwt.komuta.BaseCodec.Base.base64pad
import ro.jwt.komuta.Curve25519.POINT_BYTES

/**
 * Wraps a commutative encryption system.
 *
 * The assumption is that your message is already mapped onto a msgEmbedding
 */
class ElGamal(msgEmbedding: PublicKey, ephemeralKeys: Map<String, ByteArray> = emptyMap()) {

    val accumulator: ByteArray = ByteArray(POINT_BYTES)
    private val pubKeys: MutableMap<String, ByteArray> = mutableMapOf()

    init {
        msgEmbedding.encoded.decodeBase64().copyInto(this.accumulator)
        pubKeys.putAll(ephemeralKeys)
    }

    fun addEncryption(pubKey: PublicKey) {
        val ephemeral = Curve25519.keyPair()
        val sharedSecret = Curve25519.scalarMult(ephemeral.privateKey, pubKey.raw)
        val newAccumulator = Curve25519.add(sharedSecret, this.accumulator)
        newAccumulator.copyInto(this.accumulator)
        this.pubKeys[pubKey.encoded] = ephemeral.publicKey.raw
    }

    fun removeEncryption(keyPair: Curve25519.KeyPair) {
        val ePubKey = pubKeys[keyPair.publicKey.encoded] ?: return
        val sharedSecret = Curve25519.scalarMult(keyPair.privateKey, ePubKey)
        val newAccumulator = Curve25519.sub(this.accumulator, sharedSecret)
        newAccumulator.copyInto(this.accumulator)
        this.pubKeys.remove(keyPair.publicKey.encoded)
    }

    fun isMasked() = pubKeys.isNotEmpty()
}

inline class PublicKey(val encoded: String) {
    val raw: ByteArray
        get() = encoded.decodeBase64()

    constructor(bytes: ByteArray) : this(bytes.encodeBase64())
}

fun String.decodeBase64(): ByteArray = BaseCodec(base64pad).decode(this)
fun ByteArray.encodeBase64(): String = BaseCodec(base64pad).encode(this)