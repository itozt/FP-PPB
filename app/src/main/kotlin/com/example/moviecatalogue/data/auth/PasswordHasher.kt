package com.example.moviecatalogue.data.auth

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Minimal salted-hash helper for locally stored passwords. We never keep the
 * plain password — only a random [generateSalt] salt and the SHA-256 [hash] of
 * (salt + password). Good enough for an offline educational app.
 */
object PasswordHasher {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + password).toByteArray(Charsets.UTF_8))
        return bytes.toHex()
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
