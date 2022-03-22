package ro.jwt.komuta

fun String.decodeBase64(): ByteArray = BaseCodec(BaseCodec.Base.base64url).decode(this)
fun ByteArray.encodeBase64(): String = BaseCodec(BaseCodec.Base.base64url).encode(this)
