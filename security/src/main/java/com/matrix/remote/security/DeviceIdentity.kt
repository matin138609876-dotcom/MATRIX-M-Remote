package com.matrix.remote.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.util.UUID

class DeviceIdentity(context: Context) {

    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(
        "matrix_device_identity",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ALIAS = "matrix_device_identity_signing_key"
        private const val KEYSTORE = "AndroidKeyStore"
    }

    fun getOrCreateDeviceId(): String {
        val existing = preferences.getString("device_id", null)
        if (!existing.isNullOrBlank()) return existing

        val newId = UUID.randomUUID().toString()
        preferences.edit().putString("device_id", newId).apply()
        return newId
    }

    fun getOrCreateKeyPair(): KeyPair {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply {
            load(null)
        }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val privateKey = keyStore.getKey(KEY_ALIAS, null) as java.security.PrivateKey
            val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
            return KeyPair(publicKey, privateKey)
        }

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build()

        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    fun getPublicKeyBase64(): String {
        return Base64.encodeToString(
            getOrCreateKeyPair().public.encoded,
            Base64.NO_WRAP
        )
    }

    fun getKeyFingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(getOrCreateKeyPair().public.encoded)

        return digest.joinToString(":") {
            "%02X".format(it)
        }
    }

    fun sign(data: ByteArray): ByteArray {
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(getOrCreateKeyPair().private)
        signer.update(data)
        return signer.sign()
    }

    fun signBase64(data: ByteArray): String {
        return Base64.encodeToString(sign(data), Base64.NO_WRAP)
    }
}
