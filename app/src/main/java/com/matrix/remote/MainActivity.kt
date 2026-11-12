package com.matrix.remote

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import com.matrix.remote.security.PasswordManager
import com.matrix.remote.devices.DeviceRepository
import com.matrix.remote.devices.DeviceStatus
import com.matrix.remote.devices.MatrixDevice

class MainActivity : Activity() {

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
                .setTitle(if (firstSetup) "تنظیم رمز MATRIX M" else "ورود به MATRIX M")
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
    }    }

    private fun buildHome() {
        content.removeAllViews()

        val title = TextView(this).apply {
            text = "MATRIX M"
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
            text = "●  ${devices.size} دستگاه ثبت‌شده"
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
            contentDescription = "اتصال امن و ساخت QR"
            setOnClickListener {
                showFeature(
                    "اتصال امن",
                    "اتصال گوشی جدید",
                    "مرحله ساخت QR و اتصال واقعی در حال توسعه است."
                )
            }
        }
        content.addView(mask, LinearLayout.LayoutParams(-1, dp(190)))

        val hint = TextView(this).apply {
            text = "برای اتصال امن، نقاب را لمس کن"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(hint)
        content.addView(space(25))

        addFeature("📱", "دستگاه‌های من", "مشاهده و مدیریت گوشی‌های ثبت‌شده") {
            showDeviceManagement()
        }

        addFeature("📷", "دوربین زنده", "مشاهده دوربین گوشی دوم") {
            showFeature("دوربین زنده", "دوربین گوشی دوم", "این قابلیت هنوز پیاده‌سازی نشده است.")
        }

        addFeature("🎙️", "صدای زنده", "دریافت صدای زنده با اجازه صاحب دستگاه") {
            showFeature("صدای زنده", "میکروفون گوشی دوم", "این قابلیت هنوز پیاده‌سازی نشده است.")
        }

        addFeature("▣", "نمایشگر زنده", "مشاهده صفحه گوشی دوم") {
            showFeature("نمایشگر زنده", "اشتراک صفحه", "این قابلیت هنوز پیاده‌سازی نشده است.")
        }

        addFeature("📍", "موقعیت زنده", "موقعیت مکانی با اجازه صاحب دستگاه") {
            showFeature("موقعیت زنده", "موقعیت گوشی دوم", "این قابلیت هنوز پیاده‌سازی نشده است.")
        }

        addFeature("🔔", "اعلان‌ها", "مشاهده اعلان‌های مجاز گوشی دوم") {
            showFeature("اعلان‌ها", "اعلان‌های گوشی دوم", "این قابلیت هنوز پیاده‌سازی نشده است.")
        }

        addFeature("📁", "انتقال فایل", "انتقال فایل با اجازه صاحب دستگاه") {
            showFeature("انتقال فایل", "مدیریت فایل‌ها", "این قابلیت هنوز پیاده‌سازی نشده است.")
        }

        addFeature("⚙", "تنظیمات", "تنظیمات و مدیریت دسترسی‌ها") {
            showFeature("تنظیمات", "تنظیمات برنامه", "بخش تنظیمات در حال توسعه است.")
        }

        content.addView(space(22))

        val footer = TextView(this).apply {
            text = "MATRIX M  •  VERSION 1.0.2"
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
            text = "دستگاه‌های من"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(red)
        }
        content.addView(heading)
        content.addView(space(15))

        val addButton = TextView(this).apply {
            text = "＋  افزودن دستگاه"
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
                text = "هنوز دستگاهی ثبت نشده است."
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
            DeviceStatus.PENDING -> "در انتظار اتصال واقعی"
            DeviceStatus.CONNECTED -> "متصل"
            DeviceStatus.DISCONNECTED -> "قطع اتصال"
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

        val select = smallButton(if (selected) "انتخاب‌شده" else "انتخاب") {
            repository.selectDevice(device.id)
            showDeviceManagement()
        }

        val rename = smallButton("تغییر نام") {
            showRenameDialog(device)
        }

        val delete = smallButton("حذف") {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("حذف دستگاه")
                .setMessage("دستگاه «${device.name}» حذف شود؟")
                .setNegativeButton("انصراف", null)
                .setPositiveButton("حذف") { _, _ ->
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
            hint = "مثلاً گوشی متین"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("ثبت دستگاه جدید")
            .setMessage("نامی برای دستگاه انتخاب کن.")
            .setView(input)
            .setNegativeButton("انصراف", null)
            .setPositiveButton("ثبت") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    repository.addDevice(name)
                    showDeviceManagement()
                } else {
                    Toast.makeText(
                        this,
                        "نام دستگاه را وارد کن.",
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
            .setTitle("تغییر نام دستگاه")
            .setView(input)
            .setNegativeButton("انصراف", null)
            .setPositiveButton("ذخیره") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    repository.renameDevice(device.id, name)
                    showDeviceManagement()
                }
            }
            .show()
    }

    private fun addBackButton() {
        val back = TextView(this).apply {
            text = "‹  بازگشت"
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
