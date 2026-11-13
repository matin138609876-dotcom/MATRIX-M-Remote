package com.matrix.companion

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import com.matrix.remote.security.PasswordManager

import com.matrix.remote.pairing.PairingInvite
import com.journeyapps.barcodescanner.IntentIntegrator


class MainActivity : Activity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("trimax_q_settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "en") ?: "en"
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale(language))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    private val bg = Color.rgb(10, 15, 27)
    private val cardColor = Color.rgb(22, 30, 46)
    private val blue = Color.rgb(65, 135, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 174, 195)

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
                .setTitle(if (firstSetup) "تنظیم رمز TRIMAX Q" else "ورود به TRIMAX Q")
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

        window.statusBarColor = bg
        window.navigationBarColor = bg

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(35))
        }

        val brand = TextView(this).apply {
            text = "TRIMAX Q"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(blue)
        }
        content.addView(brand)

        val subtitle = TextView(this).apply {
            text = getString(R.string.companion_subtitle)
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(5), 0, dp(25))
        }
        content.addView(subtitle)

        val statusCard = makeCard()
        addText(statusCard, getString(R.string.device_status), 18f, white, true)
        addText(statusCard, "●  " + getString(R.string.ready_to_connect), 16f, Color.rgb(90, 220, 155), true)
        addText(statusCard, getString(R.string.no_connected_device), 13f, muted, false)
        content.addView(statusCard)

        addSection(content, getString(R.string.secure_connection))

        val connect = makeCard()
        connect.gravity = Gravity.CENTER
        connect.setBackground(cardBackground(blue, 22))
        addText(connect, "▦", 36f, white, true)
        addText(connect, getString(R.string.connect_to_main_phone), 19f, white, true)
        addText(connect, getString(R.string.scan_qr_to_connect), 13f, white, false)
        connect.setOnClickListener {
            startQrScan()
        }
        content.addView(connect)

        addSection(content, getString(R.string.device_features))

        addFeature(content, "📷", getString(R.string.camera), getString(R.string.camera_permission_detail))
        addFeature(content, "🎙", getString(R.string.microphone), getString(R.string.microphone_detail))
        addFeature(content, "▣", getString(R.string.screen_sharing), getString(R.string.screen_sharing_detail))
        addFeature(content, "📁", getString(R.string.file_transfer), getString(R.string.file_transfer_detail))

        addSection(content, getString(R.string.security_access))

        val security = makeCard()
        addText(security, "🛡  " + getString(R.string.manage_permissions), 17f, white, true)
        addText(security, getString(R.string.manage_permissions_detail), 13f, muted, false)
        security.setOnClickListener {
            Toast.makeText(this, getString(R.string.permissions_later), Toast.LENGTH_SHORT).show()
        }
        content.addView(security)

        val passwordCard = makeCard()
        addText(passwordCard, "🔐  " + getString(R.string.change_login_password), 17f, white, true)
        addText(passwordCard, getString(R.string.change_password_detail), 13f, muted, false)
        passwordCard.setOnClickListener { showPasswordChangeDialog() }
        content.addView(passwordCard)

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
                        getSharedPreferences("trimax_q_settings", MODE_PRIVATE)
                            .edit()
                            .putString("app_language", selected)
                            .apply()
                        recreate()
                    }
                    .show()
            }
        }
        content.addView(languageButton)
        content.addView(space(12))

        val footer = TextView(this).apply {
            text = "TRIMAX Q  •  VERSION 1.0.2"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(30), 0, dp(10))
        }
        content.addView(footer)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }



    private fun startQrScan() {
        IntentIntegrator(this)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            .setPrompt(getString(R.string.scan_controller_qr))
            .setBeepEnabled(false)
            .setOrientationLocked(false)
            .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val inviteText = result.contents
            if (inviteText.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.scan_cancelled), Toast.LENGTH_SHORT).show()
            } else {
                handleScannedInvite(inviteText)
            }
        }
    }

    private fun handleScannedInvite(inviteText: String) {
        if (!PairingInvite(this).verify(inviteText)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.invalid_invitation))
                .setMessage(getString(R.string.invalid_invitation_detail))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
            return
        }

        try {
            val envelope = org.json.JSONObject(inviteText)
            val payloadBytes = android.util.Base64.decode(
                envelope.getString("payload"),
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
            )
            val payload = org.json.JSONObject(String(payloadBytes, Charsets.UTF_8))
            val name = payload.getString("deviceName")
            val fingerprint = payload.getString("fingerprint")

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.connection_confirmation))
                .setMessage(getString(R.string.requesting_phone, name, fingerprint))
                .setNegativeButton(getString(R.string.reject), null)
                .setPositiveButton(getString(R.string.accept)) { _, _ ->
                    Toast.makeText(
                        this,
                        getString(R.string.invitation_accepted),
                        Toast.LENGTH_LONG
                    ).show()
                }
                .show()
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.qr_read_error), Toast.LENGTH_LONG).show()
        }
    }

    private fun showPasswordChangeDialog() {
        val manager = PasswordManager(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        fun passwordField(label: String): EditText {
            return EditText(this).apply {
                hint = label
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                textSize = 15f
            }
        }

        val current = passwordField("رمز فعلی")
        val newPassword = passwordField("رمز جدید")
        val confirm = passwordField("تکرار رمز جدید")

        layout.addView(current)
        layout.addView(newPassword)
        layout.addView(confirm)

        AlertDialog.Builder(this)
            .setTitle("تغییر رمز ورود")
            .setView(layout)
            .setNegativeButton("انصراف", null)
            .setPositiveButton("ذخیره", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val oldChars = current.text.toString().toCharArray()
                        val newChars = newPassword.text.toString().toCharArray()

                        if (newPassword.text.toString() != confirm.text.toString()) {
                            Toast.makeText(this@MainActivity, "تکرار رمز با رمز جدید یکسان نیست.", Toast.LENGTH_SHORT).show()
                        } else if (manager.changePassword(oldChars, newChars)) {
                            oldChars.fill(' ')
                            newChars.fill(' ')
                            Toast.makeText(this@MainActivity, "رمز ورود با موفقیت تغییر کرد.", Toast.LENGTH_LONG).show()
                            dismiss()
                        } else {
                            oldChars.fill(' ')
                            newChars.fill(' ')
                            Toast.makeText(this@MainActivity, "رمز فعلی اشتباه است یا رمز جدید شرایط لازم را ندارد.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                show()
            }
    }

    private fun addSection(parent: LinearLayout, title: String) {
        val view = TextView(this).apply {
            text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(dp(2), dp(25), dp(2), dp(13))
        }
        parent.addView(view)
    }

    private fun addFeature(
        parent: LinearLayout,
        icon: String,
        title: String,
        detail: String
    ) {
        val card = makeCard()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val iconView = TextView(this).apply {
            text = icon
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(blue)
            setPadding(0, 0, dp(15), 0)
        }

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        addText(labels, title, 16f, white, true)
        addText(labels, detail, 12f, muted, false)

        row.addView(iconView)
        row.addView(labels)
        card.addView(row)
        parent.addView(card)
    }

    private fun makeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = cardBackground(cardColor, 20)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dp(12)
            layoutParams = params
        }
    }

    private fun cardBackground(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.rgb(43, 58, 82))
        }
    }

    private fun addText(
        parent: LinearLayout,
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ) {
        val view = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(3), 0, dp(6))
        }
        parent.addView(view)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
