package ro.jwt.komuta

import kotlin.math.log2
import kotlin.math.roundToInt

/**
 * RFC4648 base encoder/decoder
 */
class BaseCodec(
    base: Base = Base.base64pad
) {
    private val alphabet = base.characters.toCharArray()
    private val bitsPerChar = base.bitsPerChar
    private val codes: Map<Char, Int> = this.alphabet.mapIndexed { i: Int, char: Char -> char to i }.toMap()

    fun encode(data: ByteArray): String {
        val pad = alphabet[alphabet.size - 1] == '='
        val mask = (1 shl bitsPerChar) - 1
        var out = ""
        var bits = 0
        var buffer = 0 // Bits waiting to be written out, MSB first
        for (octet in data) {
            buffer = (buffer shl 8) or octet.toUByte().toInt()
            bits += 8
            while (bits > bitsPerChar) {
                bits -= bitsPerChar
                out += alphabet[mask and (buffer shr bits)]
            }
        }
        // Partial character:
        if (bits != 0) {
            out += alphabet[mask and (buffer shl (bitsPerChar - bits))]
        }
        // Add padding characters until we hit a byte boundary:
        if (pad) {
            while ((out.length * bitsPerChar) and 7 != 0) {
                out += '='
            }
        }
        return out
    }

    fun decode(input: String): ByteArray {
        var end = input.length
        while (input[end - 1] == '=') {
            --end
        }
        val out = ByteArray(end * bitsPerChar / 8)
        var bits = 0 // Number of bits currently in the buffer
        var buffer = 0 // Bits waiting to be written out, MSB first
        var written = 0 // Next byte to write
        for (i in 0 until end) {
            val value = codes[input[i]] ?: throw IllegalArgumentException("Invalid character ${input[i]}")
            buffer = (buffer shl bitsPerChar) or value
            bits += bitsPerChar
            if (bits >= 8) {
                bits -= 8
                out[written++] = (0xff and (buffer shr bits)).toByte()
            }
        }
        if (bits >= bitsPerChar || (0xff and (buffer shl (8 - bits)) != 0)) {
            throw IllegalStateException("Unexpected end of data")
        }
        return out
    }

    @Suppress("unused")
    enum class Base(val characters: String, val bitsPerChar: Int = log2(characters.length.toFloat()).roundToInt()) {
        base2("01"),
        base8("01234567"),
        base16("0123456789abcdef"),
        base16upper("0123456789ABCDEF"),
        base32hex("0123456789abcdefghijklmnopqrstuv"),
        base32hexupper("0123456789ABCDEFGHIJKLMNOPQRSTUV"),
        base32hexpad("0123456789abcdefghijklmnopqrstuv="),
        base32hexpadupper("0123456789ABCDEFGHIJKLMNOPQRSTUV="),
        base32("abcdefghijklmnopqrstuvwxyz234567"),
        base32upper("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"),
        base32pad("abcdefghijklmnopqrstuvwxyz234567="),
        base32padupper("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567="),
        base32z("ybndrfg8ejkmcpqxot1uwisza345h769"),
        base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"),
        base64pad("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="),
        base64url("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"),
        base64urlpad("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_="),
    }

}
