package com.matrix.remote

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import java.util.Locale
import com.matrix.remote.security.PasswordManager
import com.matrix.remote.devices.DeviceRepository
import com.matrix.remote.devices.DeviceStatus
import com.matrix.remote.devices.MatrixDevice
import com.matrix.remote.pairing.PairingInvite
import com.matrix.remote.pairing.QrCodeGenerator
import android.os.Build

class MainActivity : Activity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("trimax_m_settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "en") ?: "en"
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale(language))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    private val red = Color.rgb(235, 25, 45)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 190, 200)

    private lateinit var root: FrameLayout
    private lateinit var content: LinearLayout
    private lateinit var repository: DeviceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPasswordGate()
    }

    private fun showPasswordGate() {
        val manager = PasswordManager(this)

        fun askPassword(firstSetup: Boolean) {
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 12, 48, 4)
            }
            val password = EditText(this).apply {
                hint = if (firstSetup) "رمز جدید، حداقل ۶ کاراکتر و شامل عدد" else "رمز ورود"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            box.addView(password)

            val confirm = if (firstSetup) EditText(this).apply {
                hint = "تکرار رمز"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else null
            confirm?.let { box.addView(it) }

            AlertDialog.Builder(this)
                .setTitle(if (firstSetup) "تنظیم رمز TRIMAX M" else "ورود به TRIMAX M")
                .setMessage(if (firstSetup) "برای محافظت از برنامه یک رمز تعیین کن." else "برای ادامه رمز ورود را وارد کن.")
                .setView(box)
                .setCancelable(false)
                .setPositiveButton(if (firstSetup) "ثبت رمز" else "ورود", null)
                .create().also { dialog ->
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val first = password.text.toString().toCharArray()
                            val second = confirm?.text?.toString()?.toCharArray()
                            val success = if (firstSetup) {
                                if (second == null || !first.contentEquals(second)) {
                                    false
                                } else {
                                    manager.setPassword(first)
                                }
                            } else {
                                manager.verifyPassword(first)
                            }

                            first.fill('\u0000')
                            second?.fill('\u0000')

                            if (success) {
                                dialog.dismiss()
                                initializeApp()
                            } else {
                                Toast.makeText(
                                    this,
                                    if (firstSetup) "رمزها یکسان نیستند یا رمز شرایط لازم را ندارد." else "رمز اشتباه است.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    dialog.show()
                }
        }

        askPassword(!manager.isPasswordSet())
    }

    private fun initializeApp() {

        repository = DeviceRepository(this)

        window.statusBarColor = Color.rgb(12, 5, 10)
        window.navigationBarColor = Color.BLACK

        root = FrameLayout(this)

        val background = ImageView(this).apply {
            setImageResource(R.drawable.matrix_wallpaper)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }
        root.addView(background, FrameLayout.LayoutParams(-1, -1))

        val overlay = View(this).apply {
            setBackgroundColor(Color.argb(105, 0, 0, 0))
        }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        setContentView(root)
        buildHome()
    }

    private fun buildHome() {
        content.removeAllViews()

        val title = TextView(this).apply {
            text = "TRIMAX M"
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(red)
        }
        content.addView(title)

        val subtitle = TextView(this).apply {
            text = "REMOTE DEVICE CONTROL"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(subtitle)
        content.addView(space(22))

        val devices = repository.getDevices()
        val status = TextView(this).apply {
            text = getString(R.string.registered_devices, devices.size)
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 230, 140))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = panel()
        }
        content.addView(status, matchWrap())
        content.addView(space(24))

        val mask = TextView(this).apply {
            text = "🥷"
            textSize = 76f
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
            background = transparentPanel()
            contentDescription = getString(R.string.secure_qr)
            setOnClickListener {
                showPairingScreen()
            }
        }
        content.addView(mask, LinearLayout.LayoutParams(-1, dp(190)))

        val hint = TextView(this).apply {
            text = getString(R.string.tap_mask)
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(hint)
        content.addView(space(25))

        addFeature("📱", getString(R.string.my_devices), getString(R.string.manage_devices)) {
            showDeviceManagement()
        }

        addFeature("📷", getString(R.string.live_camera), getString(R.string.second_phone_camera)) {
            showFeature(getString(R.string.live_camera), getString(R.string.second_phone_camera), getString(R.string.not_implemented))
        }

        addFeature("🎙️", getString(R.string.live_audio), getString(R.string.live_audio_detail)) {
            showFeature(getString(R.string.live_audio), getString(R.string.second_phone_microphone), getString(R.string.not_implemented))
        }

        addFeature("▣", getString(R.string.live_display), getString(R.string.live_display_detail)) {
            showFeature(getString(R.string.live_display), getString(R.string.screen_sharing), getString(R.string.not_implemented))
        }

        addFeature("📍", getString(R.string.live_location), getString(R.string.live_location_detail)) {
            showFeature(getString(R.string.live_location), getString(R.string.second_phone_location), getString(R.string.not_implemented))
        }

        addFeature("🔔", getString(R.string.notifications), getString(R.string.notifications_detail)) {
            showFeature(getString(R.string.notifications), getString(R.string.second_phone_notifications), getString(R.string.not_implemented))
        }

        addFeature("📁", getString(R.string.file_transfer), getString(R.string.file_transfer_detail)) {
            showFeature(getString(R.string.file_transfer), getString(R.string.file_management), getString(R.string.not_implemented))
        }

        addFeature("⚙", getString(R.string.settings), getString(R.string.settings_detail)) {
            showSettings()
        }

        content.addView(space(22))

        val footer = TextView(this).apply {
            text = "TRIMAX M  •  VERSION 1.0.2"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        content.addView(footer)
    }

    private fun addFeature(
        icon: String,
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = transparentPanel()
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        val symbol = TextView(this).apply {
            text = icon
            textSize = 27f
            gravity = Gravity.CENTER
        }
        item.addView(symbol, LinearLayout.LayoutParams(dp(55), dp(55)))

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        val name = TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        }

        val detail = TextView(this).apply {
            text = description
            textSize = 11f
            setTextColor(muted)
        }

        labels.addView(name)
        labels.addView(detail)
        item.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(10)
        content.addView(item, params)
    }

    private fun showDeviceManagement() {
        content.removeAllViews()

        addBackButton()
        content.addView(space(20))

        val heading = TextView(this).apply {
            text = getString(R.string.my_devices)
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(red)
        }
        content.addView(heading)
        content.addView(space(15))

        val addButton = TextView(this).apply {
            text = getString(R.string.add_device)
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = panel()
            isClickable = true
            setOnClickListener { showAddDeviceDialog() }
        }
        content.addView(addButton, matchWrap())
        content.addView(space(18))

        val devices = repository.getDevices()

        if (devices.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_devices)
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(muted)
                setPadding(dp(15), dp(25), dp(15), dp(25))
                background = transparentPanel()
            }
            content.addView(empty, matchWrap())
            return
        }

        devices.forEach { device ->
            addDeviceRow(device)
        }
    }

    private fun addDeviceRow(device: MatrixDevice) {
        val selected = repository.getSelectedDeviceId() == device.id

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = transparentPanel()
        }

        val name = TextView(this).apply {
            text = (if (selected) "✓  " else "") + device.name
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        }
        card.addView(name)

        val stateText = when (device.status) {
            DeviceStatus.PENDING -> getString(R.string.pending_connection)
            DeviceStatus.CONNECTED -> getString(R.string.connected)
            DeviceStatus.DISCONNECTED -> getString(R.string.disconnected)
        }

        val state = TextView(this).apply {
            text = stateText
            textSize = 12f
            setTextColor(muted)
        }
        card.addView(state)
        card.addView(space(10))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val select = smallButton(if (selected) getString(R.string.selected) else getString(R.string.select)) {
            repository.selectDevice(device.id)
            showDeviceManagement()
        }

        val rename = smallButton(getString(R.string.rename)) {
            showRenameDialog(device)
        }

        val delete = smallButton(getString(R.string.delete)) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.delete_device))
                .setMessage(getString(R.string.delete_device_message, device.name))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    repository.removeDevice(device.id)
                    showDeviceManagement()
                }
                .show()
        }

        actions.addView(select)
        actions.addView(rename)
        actions.addView(delete)
        card.addView(actions)

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(12)
        content.addView(card, params)
    }

    private fun smallButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(dp(9), dp(10), dp(9), dp(10))
            background = panel()
            isClickable = true
            setOnClickListener { action() }

            val params = LinearLayout.LayoutParams(0, -2, 1f)
            params.marginEnd = dp(5)
            layoutParams = params
        }
    }

    private fun showAddDeviceDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.device_name_hint)
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.new_device))
            .setMessage(getString(R.string.choose_device_name))
            .setView(input)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.register_password)) { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    repository.addDevice(name)
                    showDeviceManagement()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.enter_device_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun showRenameDialog(device: MatrixDevice) {
        val input = EditText(this).apply {
            setText(device.name)
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rename_device))
            .setView(input)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    repository.renameDevice(device.id, name)
                    showDeviceManagement()
                }
            }
            .show()
    }


    private fun showPairingScreen() {
        content.removeAllViews()
        addBackButton()
        content.addView(space(20))

        val invite = try {
            PairingInvite(this).create(Build.MODEL)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.invite_failed, e.message), Toast.LENGTH_LONG).show()
            buildHome()
            return
        }

        val heading = TextView(this).apply {
            text = getString(R.string.secure_new_phone)
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(red)
        }
        content.addView(heading)
        content.addView(space(12))

        val description = TextView(this).apply {
            text = getString(R.string.scan_invitation)
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(description)
        content.addView(space(20))

        try {
            val qr = ImageView(this).apply {
                setImageBitmap(QrCodeGenerator.generate(invite))
                setPadding(dp(8), dp(8), dp(8), dp(8))
                setBackgroundColor(Color.WHITE)
                contentDescription = getString(R.string.invitation_qr)
            }
            content.addView(qr, LinearLayout.LayoutParams(dp(280), dp(280)).apply {
                gravity = Gravity.CENTER
            })
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.qr_failed, e.message), Toast.LENGTH_LONG).show()
        }

        content.addView(space(16))
        val warning = TextView(this).apply {
            text = getString(R.string.invitation_only_warning)
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        content.addView(warning)
    }

    private fun showSettings() {
        content.removeAllViews()
        addBackButton()
        content.addView(space(35))

        val heading = TextView(this).apply {
            text = getString(R.string.settings)
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(red)
        }
        content.addView(heading)
        content.addView(space(25))

        val languageButton = Button(this).apply {
            text = getString(R.string.language) + ": " +
                if (resources.configuration.locales[0].language == "fa")
                    getString(R.string.persian) else getString(R.string.english)
            setTextColor(white)
            setOnClickListener {
                val options = arrayOf(
                    getString(R.string.english),
                    getString(R.string.persian)
                )
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.language)
                    .setItems(options) { _, which ->
                        val selected = if (which == 0) "en" else "fa"
                        getSharedPreferences("trimax_m_settings", MODE_PRIVATE)
                            .edit()
                            .putString("app_language", selected)
                            .apply()
                        recreate()
                    }
                    .show()
            }
        }
        content.addView(languageButton, matchWrap())
        content.addView(space(12))

        val changePassword = Button(this).apply {
            text = getString(R.string.change_login_password)
            setTextColor(white)
            setOnClickListener { showPasswordChangeDialog() }
        }
        content.addView(changePassword, matchWrap())
    }

    private fun showPasswordChangeDialog() {
        val manager = PasswordManager(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        fun passwordField(hintText: String): EditText {
            return EditText(this).apply {
                hint = hintText
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                textSize = 15f
            }
        }

        val current = passwordField(getString(R.string.current_password))
        val newPassword = passwordField(getString(R.string.new_password))
        val confirm = passwordField(getString(R.string.repeat_new_password))

        layout.addView(current)
        layout.addView(newPassword)
        layout.addView(confirm)

        AlertDialog.Builder(this)
            .setTitle(R.string.change_password_title)
            .setView(layout)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.save), null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val oldChars = current.text.toString().toCharArray()
                        val newChars = newPassword.text.toString().toCharArray()
                        val confirmText = confirm.text.toString()

                        if (newPassword.text.toString() != confirmText) {
                            Toast.makeText(this@MainActivity, getString(R.string.password_repeat_error), Toast.LENGTH_SHORT).show()
                        } else if (manager.changePassword(oldChars, newChars)) {
                            oldChars.fill(' ')
                            newChars.fill(' ')
                            Toast.makeText(this@MainActivity, getString(R.string.password_changed), Toast.LENGTH_LONG).show()
                            dismiss()
                        } else {
                            oldChars.fill(' ')
                            newChars.fill(' ')
                            Toast.makeText(this@MainActivity, getString(R.string.password_change_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                show()
            }
    }

    private fun addBackButton() {
        val back = TextView(this).apply {
            text = getString(R.string.back)
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(dp(12), dp(14), dp(12), dp(14))
            isClickable = true
            setOnClickListener { buildHome() }
        }
        content.addView(back, matchWrap())
    }

    private fun showFeature(title: String, subtitle: String, message: String) {
        content.removeAllViews()
        addBackButton()
        content.addView(space(45))

        val heading = TextView(this).apply {
            text = title
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(red)
        }
        content.addView(heading)
        content.addView(space(16))

        val description = TextView(this).apply {
            text = subtitle
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(description)
        content.addView(space(24))

        val info = TextView(this).apply {
            text = message
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(dp(20), dp(24), dp(20), dp(24))
            background = panel()
        }
        content.addView(info, matchWrap())
    }

    private fun transparentPanel(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.argb(155, 8, 6, 12))
            setCornerRadius(dp(18).toFloat())
            setStroke(dp(1), Color.argb(210, 235, 25, 45))
        }
    }

    private fun panel(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.argb(205, 10, 8, 14))
            setCornerRadius(dp(16).toFloat())
            setStroke(dp(1), red)
        }
    }

    private fun space(height: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(height))
        }
    }

    private fun matchWrap(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        buildHome()
    }
}
