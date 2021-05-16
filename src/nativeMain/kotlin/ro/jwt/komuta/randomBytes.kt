package ro.jwt.komuta

import kotlin.random.Random

actual fun randomBytes(size: Int): ByteArray {
    return Random.nextBytes(size)
}