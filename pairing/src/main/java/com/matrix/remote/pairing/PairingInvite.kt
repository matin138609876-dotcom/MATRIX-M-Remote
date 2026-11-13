package com.matrix.remote.pairing

import android.content.Context
import android.util.Base64
import com.matrix.remote.security.DeviceIdentity
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.security.SecureRandom

class PairingInvite(context: Context) {

    private val identity = DeviceIdentity(context.applicationContext)

    companion object {
        private const val VERSION = 1
        private const val VALIDITY_MS = 5 * 60 * 1000L
    }

    fun create(deviceName: String): String {
        require(deviceName.isNotBlank()) { "Device name is required" }

        val now = System.currentTimeMillis()
        val nonceBytes = ByteArray(32)
        SecureRandom().nextBytes(nonceBytes)

        val payload = JSONObject().apply {
            put("version", VERSION)
            put("deviceId", identity.getOrCreateDeviceId())
            put("deviceName", deviceName.trim())
            put("publicKey", identity.getPublicKeyBase64())
            put("fingerprint", identity.getKeyFingerprint())
            put("createdAt", now)
            put("expiresAt", now + VALIDITY_MS)
            put("nonce", Base64.encodeToString(nonceBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        }

        val payloadBytes = payload.toString().toByteArray(Charsets.UTF_8)
        val signature = identity.sign(payloadBytes)

        return JSONObject().apply {
            put("payload", Base64.encodeToString(payloadBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
            put("signature", Base64.encodeToString(signature, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        }.toString()
    }

    fun verify(inviteText: String): Boolean {
        return try {
            val envelope = JSONObject(inviteText)
            val payloadBytes = Base64.decode(
                envelope.getString("payload"),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val signatureBytes = Base64.decode(
                envelope.getString("signature"),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
            if (payload.getInt("version") != VERSION) return false
            if (payload.getLong("expiresAt") <= System.currentTimeMillis()) return false
            if (payload.getLong("createdAt") > System.currentTimeMillis() + 60_000L) return false

            val publicKeyBytes = Base64.decode(
                payload.getString("publicKey"),
                Base64.DEFAULT
            )
            val publicKey = KeyFactory.getInstance("EC")
                .generatePublic(X509EncodedKeySpec(publicKeyBytes))

            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(payloadBytes)

            verifier.verify(signatureBytes) &&
                payload.getString("fingerprint") == fingerprint(publicKeyBytes) &&
                payload.getString("deviceId").isNotBlank() &&
                payload.getString("nonce").isNotBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun fingerprint(publicKey: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(publicKey)
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
