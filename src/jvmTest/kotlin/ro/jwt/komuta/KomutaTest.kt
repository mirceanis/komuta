package ro.jwt.komuta

import org.junit.Assert
import org.junit.Test
import java.util.*

class KomutaTest {

    /**
     * A few curve points usable as prebaked message embeddings
     */
    val cardEmbeddings = arrayOf(
        "V5aZXdL2bfzyXqnwcYCsHpa5ojGKxc7Nh9vMQIrnvlY=",
        "27kXjSX9x+O6AT/uf/KHBwdlE/5pUHj3JOxXmh9SsiE=",
        "M2d/3YvGrWzDAUQ5wckpaYB/od85NEzPN4DohhI0d3Y=",
        "eVkpC5mUtBLogawnkBwPDGMpwq5UheJf9LrY04GwHAU="
    )

    @Test
    fun `decode base64`() {
        for (i in 0..365) {
            val input = randomBytes(32)
            val encoded = Base64.getEncoder().encodeToString(input)
            val decoded = BaseCodec(BaseCodec.Base.base64pad).decode(encoded)
            Assert.assertArrayEquals(input, decoded)
        }
    }

    @Test
    fun `encode base64`() {
        for (i in 0..365) {
            val input = randomBytes(32)
            val encoded = BaseCodec(BaseCodec.Base.base64pad).encode(input)
            val expected = Base64.getEncoder().encodeToString(input)
            Assert.assertEquals(expected, encoded)
        }
    }

    @Test
    fun `add and subtract`() {
        val card1 = cardEmbeddings[1].decodeBase64()
        val card2 = cardEmbeddings[2].decodeBase64()
        val result = Curve25519.add(card1, card2)
        val inverse = Curve25519.sub(result, card2)
        Assert.assertArrayEquals(inverse, card1)
        Assert.assertEquals(cardEmbeddings[1], inverse.encodeBase64())
    }

    @Test
    fun `single encrypt card`() {
        val card1 = cardEmbeddings[1]
        val maskedCard = Komuta(PublicKey(card1))
        val aliceKeypair = Curve25519.keyPair()

        maskedCard.addEncryption(aliceKeypair.publicKey)
        Assert.assertNotEquals(card1, maskedCard.accumulator.encodeBase64())
        maskedCard.removeEncryption(aliceKeypair)

        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }

    @Test
    fun `double encrypt card, decrypt LIFO`() {
        val card1 = cardEmbeddings[1]
        val maskedCard = Komuta(PublicKey(card1))
        val aliceKeypair = Curve25519.keyPair()
        val bobKeypair = Curve25519.keyPair()

        maskedCard.addEncryption(aliceKeypair.publicKey)
        maskedCard.addEncryption(bobKeypair.publicKey)
        maskedCard.removeEncryption(bobKeypair)
        maskedCard.removeEncryption(aliceKeypair)

        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }

    @Test
    fun `double encrypt card, decrypt FIFO`() {
        val card1 = cardEmbeddings[1]
        val maskedCard = Komuta(PublicKey(card1))
        val aliceKeypair = Curve25519.keyPair()
        val bobKeypair = Curve25519.keyPair()

        maskedCard.addEncryption(aliceKeypair.publicKey)
        maskedCard.addEncryption(bobKeypair.publicKey)
        maskedCard.removeEncryption(aliceKeypair)
        maskedCard.removeEncryption(bobKeypair)

        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }

    @Test
    fun `triple encrypt card, with serialization, decrypt radnom order`() {
        val card1 = cardEmbeddings[1]
        var maskedCard = Komuta(PublicKey(card1))

        val aliceKeypair = Curve25519.keyPair()
        val bobKeypair = Curve25519.keyPair()
        val charlieKeypair = Curve25519.keyPair()

        //add 3 encryption layers and serialize
        maskedCard.addEncryption(aliceKeypair.publicKey)
        maskedCard.addEncryption(bobKeypair.publicKey)
        maskedCard.addEncryption(charlieKeypair.publicKey)
        Assert.assertTrue(maskedCard.isMasked())
        val toBob = maskedCard.toString()

        //bob decrypts
        maskedCard = Komuta.fromString(toBob)
        maskedCard.removeEncryption(bobKeypair)
        val toAlice = maskedCard.toString()
        //alice decrypts
        maskedCard = Komuta.fromString(toAlice)
        maskedCard.removeEncryption(aliceKeypair)
        val toCharlie = maskedCard.toString()
        //charlie decrypts
        maskedCard = Komuta.fromString(toCharlie)
        maskedCard.removeEncryption(charlieKeypair)

        Assert.assertFalse(maskedCard.isMasked())

        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }
}