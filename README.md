# komuta

A commutative encryption implementation.

This is a naive implementation of commutative encryption using the ElGamal scheme applied over Curve25519.
The purpose is mental poker.

## Disclaimer
This code is not audited, don't use it to secure your secrets.

## Usage

First, you need an embedding of your message onto a Curve25519 point.
If your message space is low and known to all parties (for example, the indices of a cards in a deck),
each party could do something like this:

```kotlin
val msg: ByteArray = // one of your possible messages

fun yourEmbeddingFunction(msg: ByteArray): PublicKey {
    val pair = Curve25519.keyPairFromScalar(msg)
    return PublicKey(pair.publicKey)
}
```

### Encryption step

```kotlin
val embeddedMessage = yourEmbeddingFunction(msg)
val maskedMessage = Komuta(embeddedMessage)
```

alice, bob, charlie, etc each create their keypairs and share public keys

encryption can be added using only the public keys

```kotlin

val aliceKeypair = Curve25519.keyPair()
val bobKeypair = Curve25519.keyPair()
val charlieKeypair = Curve25519.keyPair()

maskedMessage.addEncryption(aliceKeypair.publicKey)
maskedMessage.addEncryption(bobKeypair.publicKey)
maskedMessage.addEncryption(charlieKeypair.publicKey)

val serializedMaskedMessage = maskedMessage.toString()
```

### Decryption step

Each party uses their keypair to remove their layer of encryption.

**The order in which they remove it does not matter**

```kotlin
// Alice's machine
val maskedMessage = Komuta.fromString(serializedMaskedMessage)
maskedMessage.removeEncryption(aliceKeypair)
val serializedToCharlie = maskedMessage.toString()

// Charlie's machine
val maskedMessage = Komuta.fromString(serializedToCharlie)
maskedMessage.removeEncryption(charlieKeypair)
val serializedToBob = maskedMessage.toString()

// Bob's machine
val maskedMessage = Komuta.fromString(serializedToBob)
maskedMessage.removeEncryption(bobKeypair)

//after removing all encryption layers, the accumulator represents the embedded message.
Assert.assertArrayEquals(embeddedMessage.decodeBase64(), maskedMessage.accumulator)
```
