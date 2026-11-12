package com.matrix.companion

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

class MainActivity : Activity() {

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
            text = "MATRIX M"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(blue)
        }
        content.addView(brand)

        val subtitle = TextView(this).apply {
            text = "COMPANION  •  همراه هوشمند دستگاه"
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(5), 0, dp(25))
        }
        content.addView(subtitle)

        val statusCard = makeCard()
        addText(statusCard, "وضعیت دستگاه", 18f, white, true)
        addText(statusCard, "●  آماده اتصال", 16f, Color.rgb(90, 220, 155), true)
        addText(statusCard, "هنوز هیچ دستگاهی متصل نشده است.", 13f, muted, false)
        content.addView(statusCard)

        addSection(content, "اتصال امن")

        val connect = makeCard()
        connect.gravity = Gravity.CENTER
        connect.setBackground(cardBackground(blue, 22))
        addText(connect, "▦", 36f, white, true)
        addText(connect, "اتصال به گوشی اصلی", 19f, white, true)
        addText(connect, "اسکن کد QR برای شروع اتصال", 13f, white, false)
        connect.setOnClickListener {
            Toast.makeText(this, "اسکن QR در مرحله بعد پیاده‌سازی می‌شود.", Toast.LENGTH_SHORT).show()
        }
        content.addView(connect)

        addSection(content, "قابلیت‌های دستگاه")

        addFeature(content, "📷", "دوربین", "فعال‌سازی فقط با اجازه صاحب گوشی")
        addFeature(content, "🎙", "میکروفون", "استفاده با مجوز و اعلان اندروید")
        addFeature(content, "▣", "اشتراک‌گذاری صفحه", "با تأیید پنجره امنیتی اندروید")
        addFeature(content, "📁", "انتقال فایل", "ارسال و دریافت فایل با تأیید کاربر")

        addSection(content, "امنیت و دسترسی")

        val security = makeCard()
        addText(security, "🛡  مدیریت دسترسی‌ها", 17f, white, true)
        addText(security, "مشاهده و لغو مجوزهای برنامه", 13f, muted, false)
        security.setOnClickListener {
            Toast.makeText(this, "مدیریت دسترسی‌ها در مرحله بعد اضافه می‌شود.", Toast.LENGTH_SHORT).show()
        }
        content.addView(security)

        val footer = TextView(this).apply {
            text = "MATRIX M COMPANION  •  VERSION 1.0.2"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(30), 0, dp(10))
        }
        content.addView(footer)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }    }

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
