package ro.jwt.komuta

import Komuta
import org.junit.Assert
import org.junit.Test
import java.util.*

class KomutaTest {

    private val cardToPoint = mutableMapOf<Int, String>()
    private val pointToCard = mutableMapOf<String, Int>()

    /**
     * A few curve points usable as pre-baked message embeddings
     * This could represent a deck of cards
     */
    private val cardEmbeddings = (0 until 52).map { cardIndex ->
        val scalar = byteArrayOf(42, cardIndex.toByte())
        val point = Curve25519.keyPairFromScalar(scalar).publicKey.serialize()
        cardToPoint[cardIndex] = point
        pointToCard[point] = cardIndex
        point
    }

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
    fun `mask and unmask a card`() {
        val card1 = cardEmbeddings[1]
        val aliceKeypair = Curve25519.keyPair()

        val maskedCard = Komuta(pk = aliceKeypair.publicKey, msg = Point(card1)).mask()

        Assert.assertEquals(true, maskedCard.isMasked())

        Assert.assertNotEquals(card1, maskedCard.msg.serialize())
        val unmaskedCard = maskedCard.unmask(aliceKeypair.privateKey)
        Assert.assertEquals(false, unmaskedCard.isMasked())

        Assert.assertEquals(card1, unmaskedCard.msg.serialize())
    }


    @Test
    fun `double encrypt card, decrypt LIFO`() {
        val card1 = cardEmbeddings[1]
        val aliceKeypair = Curve25519.keyPair()
        val pk = aliceKeypair.publicKey

        val maskedCard = Komuta(pk = pk, msg = Point(card1))
            .mask()
            .mask()

        Assert.assertNotEquals(card1, maskedCard.msg.serialize())

        val unmaskedCard = maskedCard
            .unmask(aliceKeypair.privateKey)

        Assert.assertEquals(card1, unmaskedCard.msg.serialize())

//        val card1 = cardEmbeddings[1]
//        val maskedCard = Komutaz(PublicKey(card1))
//        val aliceKeypair = Curve25519.keyPair()
//        val bobKeypair = Curve25519.keyPair()
//
//        maskedCard.addEncryption(aliceKeypair.publicKey)
//        maskedCard.addEncryption(bobKeypair.publicKey)
//        maskedCard.removeEncryption(bobKeypair)
//        maskedCard.removeEncryption(aliceKeypair)
//
//        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }

//    @Test
//    fun `double encrypt card, decrypt FIFO`() {
//        val card1 = cardEmbeddings[1]
//        val maskedCard = Komutaz(PublicKey(card1))
//        val aliceKeypair = Curve25519.keyPair()
//        val bobKeypair = Curve25519.keyPair()
//
//        maskedCard.addEncryption(aliceKeypair.publicKey)
//        maskedCard.addEncryption(bobKeypair.publicKey)
//        maskedCard.removeEncryption(aliceKeypair)
//        maskedCard.removeEncryption(bobKeypair)
//
//        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
//    }
//
//    @Test
//    fun `triple encrypt card, with serialization, decrypt random order`() {
//        val card1 = 1
//        val embeddedCard = cardToPoint[card1]!!
//        var maskedCard = Komutaz(PublicKey(embeddedCard))
//
//        val aliceKeypair = Curve25519.keyPair()
//        val bobKeypair = Curve25519.keyPair()
//        val charlieKeypair = Curve25519.keyPair()
//
//        //add 3 encryption layers and serialize
//        maskedCard.addEncryption(aliceKeypair.publicKey)
//        maskedCard.addEncryption(bobKeypair.publicKey)
//        maskedCard.addEncryption(charlieKeypair.publicKey)
//        val toBob = maskedCard.toString()
//
//        Assert.assertTrue(maskedCard.isMasked())
//        Assert.assertTrue(pointToCard.containsKey(embeddedCard))
//        Assert.assertFalse(pointToCard.containsKey(maskedCard.accumulator.encodeBase64()))
//
//        //bob decrypts
//        maskedCard = Komutaz.fromString(toBob)
//        maskedCard.removeEncryption(bobKeypair)
//        val toAlice = maskedCard.toString()
//        //alice decrypts
//        maskedCard = Komutaz.fromString(toAlice)
//        maskedCard.removeEncryption(aliceKeypair)
//        val toCharlie = maskedCard.toString()
//        //charlie decrypts
//        maskedCard = Komutaz.fromString(toCharlie)
//        maskedCard.removeEncryption(charlieKeypair)
//
//        Assert.assertFalse(maskedCard.isMasked())
//        val recoveredCard = pointToCard[maskedCard.accumulator.encodeBase64()]
//        Assert.assertEquals(card1, recoveredCard)
//    }
}