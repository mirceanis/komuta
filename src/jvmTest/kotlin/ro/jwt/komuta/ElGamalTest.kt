package ro.jwt.komuta

import org.junit.Assert
import org.junit.Test
import java.util.*

class ElGamalTest {

    /**
     * A few curve points usable as prebaked message embeddings
     */
    val cardEmbeddings = arrayOf(
        "V5aZXdL2bfzyXqnwcYCsHpa5ojGKxc7Nh9vMQIrnvlY=",
        "27kXjSX9x+O6AT/uf/KHBwdlE/5pUHj3JOxXmh9SsiE=",
        "M2d/3YvGrWzDAUQ5wckpaYB/od85NEzPN4DohhI0d3Y=",
        "eVkpC5mUtBLogawnkBwPDGMpwq5UheJf9LrY04GwHAU=",
        "95/YArCCU0p6PCjCFQaUIB9w2ax+SJy4IcY7ppeOEwE=",
        "LtuSTC8/MZNA8dandl7A8fuk9vJnSWq3v68Y3obqt0s=",
        "JnrPNZVZ+3zKr8OtFLPIIl/VO9K3YyUU/PP284R4kmA=",
        "3X57XNe9NqRs2Yxxyjzc/aL/MHUgFdkqYRCsyZnSc1A=",
        "zYhOljmZtaP9z0kucJMoJxx6AecReBuxD+EhkIrnCiw=",
        "IVV10abxtkZQxOO8adugoZ1uH5DzjYg7VYA/udTliic=",
        "LO9QRSHy3ORR0U57yQrbcgwwnOAtHcYDgF9MMvi+lGg=",
        "yIVOPXpK1z8BGOBkOrg9aCodAi9k1oWrjOJ5yV98t3w=",
        "QGN4DuymVbf7X6t8uAPhjfEXDk7NxDJaUB7KpfWtaTY=",
        "Bruayfaofu0Et5HR/QwZqrQsE9l9M294M844f6VX4gk=",
        "MrP1XQJzESpHZAwzTLCfHk4p5WViGvs9WVNLng8EUk4=",
        "sCZP7CWiaP8g4Z50KWMIdbIWMDDUtg7GXt/niP/ItRQ=",
        "xXQp1ori74ZxFIZU4GLo/bVDfvu9EZqnR/qlwI4vGGc=",
        "XkWntS8ntornAMoP/0i/trfygyyTL+7KFgd+9iBoUSY=",
        "JtabPn2HD4l8q6p4CZOxUH87LKEBtKzWBeQzmBH4Py4=",
        "syHSxmXUcFMrWyUhv0en7rLAufavMhrqbi5gqmVumw4=",
        "xW0VH9ZHu0rA4cJa/nvgjxr8om8r+KdHhKD8fDHW51w=",
        "6AuM54GLoyQ+QO2b+fNOLdwQX6K2LYDVaWcT1Qjj+VE=",
        "KxzI17dkPqiIO+o+RroxxRTdRS/vR6E1CmEJK7r/M1s=",
        "xDz2lYzeXsr+rtp/4OqsBO9R7cKnsRu4nWkx7ZUpYnM=",
        "59001wYBNxUK92yYzzwIp9QWf3vt/4FRuB3MoxYB9ms=",
        "y6/IeLUA8mCCnSEzsrFmJDGwXO/alBAow6a3ZM1SFzo=",
        "IT7gjmIZEg8N0m/yK3xxLB2D9dR8Ir1fuWOt9hRpBmM=",
        "bHDspsi0GB9wXwcDFTlnp7s0dpqF+EuT7inlETkFAk8=",
        "6385dU6rLhdcm0cBSP8GPH593NMhQbv0B4TUM6N/VkU=",
        "37kI4fWJLaMDQnMhG5UgYHTPxnUuv5kO458hogYTVko=",
        "yJhEQRXp3Fg10E+hNWDhfQJngsmmy8Gh6w/RBzXfFzg=",
        "HYorkY9fWfHQrWt3PPvzOvfImTmvWSnKS5l5mioZ+2o=",
        "G+x0KlQxfOsUjAw/DX05H8Wl5n+h6m/yI47brnNWjzM=",
        "P4kw1ut8b3OYa5T0YzzconCJKAm2QoAMkk3j+kHtsx0=",
        "zcSRITyOBI4u+sztoxjcm2M8TtUz0i7feKS3LcwgcBg=",
        "A8hFRONXFO6+hTcg2X5rZiBYEi6VBI/5y6TVB1V3zUA=",
        "8NfhSmWW5Qsom6qqYheUuDt+4mVCfzguCnJ0x8II3g8=",
        "eMHUNff5qDHcn+rlQKgtxAvdXNKWmS59QEppad1BGhs=",
        "lOIkufqJeBADaAPDoK74v+wTVHEtkeVNnG43YP624kM=",
        "P8CQ8cYGDk5xvCDzgpZwcZ23ZjSGTM75ajdTfMVkxA4=",
        "J8bS9INvpeIHXeb3Dt2l8bOia4Grkadc56bD7c905GM=",
        "VpdzXxPjhIBTz+IElMi7MvYsWrEVPstUwjYYSunY2CU=",
        "kX51QQ7vJCTZMBBrJvQ1gVoM8BaKJWMyfztt/KcsR0o=",
        "FKUakCO9VMIreLw4nzrzhl8i19zQagJk1ZtjFVhAqBQ=",
        "OaHSays1igMzmUW4T80FJTccDrsajw16KeFcvJ+v6EQ=",
        "BFDeZg7jZ5Xh/oqEBj4nxToETnvafrz+b/NOAQf4jzg=",
        "+sbFiEAjXvrCwrgfdrr4F0zjLAY759RbsWdGPJj00CI=",
        "yi+nd5KYEzUlD33Nt4QHP3umyPpNO7iUfdn1oWO4CCM=",
        "MSZtjqHs4DVwwc03CDrXeljQGWkFsCWBlCCV7tT/Nlg=",
        "13HJM0BX1RA5YgXnKgpRW74YJ6C6y834+h6KfxUUATY=",
        "qoLg239d8X/1faDg2lFskwBbm0MDuBCb+B82ObQYNEM=",
        "Cu3uEwISn2UqfR9CN+i+ov2c+zCMw8ZZ57oIhuYUc00=",
        "B2T0GOU5ntOXQ8yXRkuMbjCOD0/JHtFmcuqXJPloGGo=",
        "TWzCvrQ7J0s2/FZgT2kaTI6Mi+XmN4MLA84ZSAx1xnU=",
        "h74vgFYx3XvZsrSSev7ulvVGupYls4pI0ChDstDhZGo="
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
        val maskedCard = ElGamal(PublicKey(card1))
        val aliceKeypair = Curve25519.keyPair()

        maskedCard.addEncryption(aliceKeypair.publicKey)
        Assert.assertNotEquals(card1, maskedCard.accumulator.encodeBase64())
        maskedCard.removeEncryption(aliceKeypair)

        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }

    @Test
    fun `double encrypt card, decrypt LIFO`() {
        val card1 = cardEmbeddings[1]
        val maskedCard = ElGamal(PublicKey(card1))
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
        val maskedCard = ElGamal(PublicKey(card1))
        val aliceKeypair = Curve25519.keyPair()
        val bobKeypair = Curve25519.keyPair()

        maskedCard.addEncryption(aliceKeypair.publicKey)
        maskedCard.addEncryption(bobKeypair.publicKey)
        maskedCard.removeEncryption(aliceKeypair)
        maskedCard.removeEncryption(bobKeypair)

        Assert.assertArrayEquals(card1.decodeBase64(), maskedCard.accumulator)
    }
}