package ro.jwt.komuta

import java.security.SecureRandom

actual fun randomBytes(size: Int): ByteArray {
    val output = ByteArray(size)
    SecureRandom.getInstanceStrong().nextBytes(output)
    return output
}