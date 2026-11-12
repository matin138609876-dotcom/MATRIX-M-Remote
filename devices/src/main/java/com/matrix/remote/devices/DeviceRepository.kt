package com.matrix.remote.devices

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class DeviceStatus {
    PENDING,
    CONNECTED,
    DISCONNECTED
}

data class MatrixDevice(
    val id: String,
    val name: String,
    val status: DeviceStatus,
    val addedAt: Long,
    val lastSeen: Long
)

class DeviceRepository(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        "matrix_devices",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val DEVICES_KEY = "devices"
        private const val SELECTED_KEY = "selected_device"
    }

    @Synchronized
    fun getDevices(): List<MatrixDevice> {
        val result = mutableListOf<MatrixDevice>()
        val json = preferences.getString(DEVICES_KEY, "[]") ?: "[]"

        try {
            val array = JSONArray(json)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                result.add(
                    MatrixDevice(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        status = runCatching {
                            DeviceStatus.valueOf(item.getString("status"))
                        }.getOrDefault(DeviceStatus.DISCONNECTED),
                        addedAt = item.getLong("addedAt"),
                        lastSeen = item.getLong("lastSeen")
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }

        return result
    }

    @Synchronized
    fun addDevice(name: String): MatrixDevice {
        require(name.isNotBlank()) { "Device name cannot be empty" }

        val now = System.currentTimeMillis()

        val device = MatrixDevice(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            status = DeviceStatus.PENDING,
            addedAt = now,
            lastSeen = 0L
        )

        val devices = getDevices().toMutableList()
        devices.add(device)
        saveDevices(devices)

        return device
    }

    @Synchronized
    fun updateStatus(id: String, status: DeviceStatus): Boolean {
        var updated = false

        val devices = getDevices().map { device ->
            if (device.id == id) {
                updated = true
                device.copy(
                    status = status,
                    lastSeen = if (status == DeviceStatus.CONNECTED) {
                        System.currentTimeMillis()
                    } else {
                        device.lastSeen
                    }
                )
            } else {
                device
            }
        }

        if (updated) saveDevices(devices)
        return updated
    }

    @Synchronized
    fun renameDevice(id: String, newName: String): Boolean {
        if (newName.isBlank()) return false

        var updated = false

        val devices = getDevices().map { device ->
            if (device.id == id) {
                updated = true
                device.copy(name = newName.trim())
            } else {
                device
            }
        }

        if (updated) saveDevices(devices)
        return updated
    }

    @Synchronized
    fun removeDevice(id: String): Boolean {
        val devices = getDevices()
        val filtered = devices.filterNot { it.id == id }

        if (filtered.size == devices.size) return false

        saveDevices(filtered)

        if (getSelectedDeviceId() == id) {
            preferences.edit().remove(SELECTED_KEY).apply()
        }

        return true
    }

    @Synchronized
    fun selectDevice(id: String): Boolean {
        if (getDevices().none { it.id == id }) return false

        preferences.edit().putString(SELECTED_KEY, id).apply()
        return true
    }

    fun getSelectedDeviceId(): String? {
        return preferences.getString(SELECTED_KEY, null)
    }

    fun getSelectedDevice(): MatrixDevice? {
        val id = getSelectedDeviceId() ?: return null
        return getDevices().firstOrNull { it.id == id }
    }

    private fun saveDevices(devices: List<MatrixDevice>) {
        val array = JSONArray()

        devices.forEach { device ->
            array.put(
                JSONObject().apply {
                    put("id", device.id)
                    put("name", device.name)
                    put("status", device.status.name)
                    put("addedAt", device.addedAt)
                    put("lastSeen", device.lastSeen)
                }
            )
        }

        preferences.edit()
            .putString(DEVICES_KEY, array.toString())
            .apply()
    }
}
