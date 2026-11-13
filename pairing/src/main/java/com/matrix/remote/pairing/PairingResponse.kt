package com.matrix.remote.pairing

import android.content.Context
import android.util.Base64
import com.matrix.remote.security.DeviceIdentity
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class PairingResponse(context: Context) {

    private val identity = DeviceIdentity(context.applicationContext)

    companion object {
        private const val VERSION = 1
        private const val VALIDITY_MS = 5 * 60 * 1000L
        private const val FLAGS =
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    }

    fun create(inviteText: String, deviceName: String): String {
        require(deviceName.isNotBlank()) { "Device name is required" }

        val invite = readAndVerifyInvite(inviteText)
        val now = System.currentTimeMillis()

        val payload = JSONObject().apply {
            put("version", VERSION)
            put("controllerDeviceId", invite.getString("deviceId"))
            put("inviteNonce", invite.getString("nonce"))
            put("deviceId", identity.getOrCreateDeviceId())
            put("deviceName", deviceName.trim())
            put("publicKey", identity.getPublicKeyBase64())
            put("fingerprint", identity.getKeyFingerprint())
            put("createdAt", now)
            put("expiresAt", now + VALIDITY_MS)
        }

        val bytes = payload.toString().toByteArray(Charsets.UTF_8)
        val signature = identity.sign(bytes)

        return JSONObject().apply {
            put("payload", Base64.encodeToString(bytes, FLAGS))
            put("signature", Base64.encodeToString(signature, FLAGS))
        }.toString()
    }

    fun verify(
        responseText: String,
        expectedControllerId: String,
        expectedNonce: String
    ): Boolean {
        return try {
            val envelope = JSONObject(responseText)
            val bytes = Base64.decode(envelope.getString("payload"), FLAGS)
            val signatureBytes =
                Base64.decode(envelope.getString("signature"), FLAGS)
            val payload = JSONObject(String(bytes, Charsets.UTF_8))
            val now = System.currentTimeMillis()

            if (payload.getInt("version") != VERSION) return false
            if (payload.getLong("expiresAt") <= now) return false
            if (payload.getLong("createdAt") > now + 60_000L) return false
            if (payload.getLong("expiresAt") - payload.getLong("createdAt") > VALIDITY_MS) return false
            if (payload.getString("controllerDeviceId") != expectedControllerId) return false
            if (payload.getString("inviteNonce") != expectedNonce) return false

            val publicKeyBytes = Base64.decode(
                payload.getString("publicKey"),
                Base64.DEFAULT
            )
            val publicKey = KeyFactory.getInstance("EC")
                .generatePublic(X509EncodedKeySpec(publicKeyBytes))

            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(bytes)

            verifier.verify(signatureBytes) &&
                payload.getString("fingerprint") == fingerprint(publicKeyBytes) &&
                payload.getString("deviceId").isNotBlank() &&
                payload.getString("deviceName").isNotBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun readAndVerifyInvite(text: String): JSONObject {
        require(PairingInvite(context = identityContext()).verify(text)) {
            "Invalid or expired invitation"
        }

        val envelope = JSONObject(text)
        val bytes = Base64.decode(envelope.getString("payload"), FLAGS)
        return JSONObject(String(bytes, Charsets.UTF_8))
    }

    private fun identityContext(): Context = contextHolder
        ?: throw IllegalStateException("Context unavailable")

    private var contextHolder: Context? = null

    private fun fingerprint(publicKey: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(publicKey)
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
