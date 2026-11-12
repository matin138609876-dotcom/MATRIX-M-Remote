package com.matrix.remote.security

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "matrix_password_security",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val SALT_KEY = "password_salt"
        private const val HASH_KEY = "password_hash"
        private const val ITERATIONS = 210_000
        private const val KEY_LENGTH = 256
    }

    fun isPasswordSet(): Boolean {
        return prefs.contains(SALT_KEY) && prefs.contains(HASH_KEY)
    }

    @Synchronized
    fun setPassword(password: CharArray): Boolean {
        if (!isValidPassword(password)) return false

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val hash = hashPassword(password, salt)

        return prefs.edit()
            .putString(SALT_KEY, bytesToHex(salt))
            .putString(HASH_KEY, bytesToHex(hash))
            .commit()
    }

    fun verifyPassword(password: CharArray): Boolean {
        val saltText = prefs.getString(SALT_KEY, null) ?: return false
        val hashText = prefs.getString(HASH_KEY, null) ?: return false

        return try {
            val salt = hexToBytes(saltText)
            val expectedHash = hexToBytes(hashText)
            val actualHash = hashPassword(password, salt)
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun changePassword(currentPassword: CharArray, newPassword: CharArray): Boolean {
        if (!verifyPassword(currentPassword)) return false
        return setPassword(newPassword)
    }

    private fun isValidPassword(password: CharArray): Boolean {
        return password.size in 6..128 && password.any { it.isDigit() }
    }

    private fun hashPassword(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(value: String): ByteArray {
        require(value.length % 2 == 0)
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
