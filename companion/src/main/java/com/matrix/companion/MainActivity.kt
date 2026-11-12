package com.matrix.companion

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private val bg = Color.rgb(246, 247, 249)
    private val textColor = Color.rgb(30, 35, 42)
    private val subColor = Color.rgb(110, 117, 126)
    private val blue = Color.rgb(45, 105, 210)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 30)
        }

        val title = TextView(this).apply {
            text = "MATRIX M"
            textSize = 27f
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
        }
        content.addView(title)

        val subtitle = TextView(this).apply {
            text = "همراه دستگاه"
            textSize = 15f
            setTextColor(subColor)
            setPadding(0, 5, 0, 25)
        }
        content.addView(subtitle)

        addSection(content, "وضعیت دستگاه")
        addInfoCard(content, "●  آماده اتصال", "هنوز هیچ دستگاهی متصل نشده است.")

        addSection(content, "اتصال")
        addActionCard(content, "اتصال به گوشی اصلی", "اسکن کد QR برای اتصال امن") {
            Toast.makeText(this, "قابلیت اتصال در مرحله بعد ساخته می‌شود.", Toast.LENGTH_SHORT).show()
        }

        addSection(content, "قابلیت‌های دستگاه")
        addSettingCard(content, "📷", "دوربین", "فعال‌سازی فقط با اجازه صاحب گوشی")
        addSettingCard(content, "🎙", "میکروفون", "فعال‌سازی با مجوز و اعلان اندروید")
        addSettingCard(content, "▣", "اشتراک‌گذاری صفحه", "با تأیید پنجره امنیتی اندروید")
        addSettingCard(content, "📁", "انتقال فایل", "ارسال و دریافت فایل با تأیید کاربر")

        addSection(content, "امنیت و دسترسی")
        addActionCard(content, "مدیریت دسترسی‌ها", "مشاهده و لغو مجوزهای برنامه") {
            Toast.makeText(this, "مدیریت دسترسی‌ها به‌زودی اضافه می‌شود.", Toast.LENGTH_SHORT).show()
        }

        val footer = TextView(this).apply {
            text = "MATRIX M Companion  •  نسخه 1.0.0"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(subColor)
            setPadding(0, 28, 0, 10)
        }
        content.addView(footer)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun addSection(parent: LinearLayout, title: String) {
        val view = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            setPadding(2, 22, 2, 12)
        }
        parent.addView(view)
    }

    private fun addInfoCard(parent: LinearLayout, title: String, detail: String) {
        val card = makeCard()
        addText(card, title, 16f, textColor, true)
        addText(card, detail, 13f, subColor, false)
        parent.addView(card)
    }

    private fun addActionCard(parent: LinearLayout, title: String, detail: String, action: () -> Unit) {
        val card = makeCard()
        card.setOnClickListener { action() }
        addText(card, title, 16f, blue, true)
        addText(card, detail, 13f, subColor, false)
        parent.addView(card)
    }

    private fun addSettingCard(parent: LinearLayout, icon: String, title: String, detail: String) {
        val card = makeCard()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val iconView = TextView(this).apply {
            text = icon
            textSize = 23f
            setPadding(0, 0, 15, 0)
        }

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val name = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
        }

        val description = TextView(this).apply {
            text = detail
            textSize = 12f
            setTextColor(subColor)
            setPadding(0, 4, 0, 0)
        }

        labels.addView(name)
        labels.addView(description)
        row.addView(iconView)
        row.addView(labels)
        card.addView(row)
        parent.addView(card)
    }

    private fun makeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(17, 17, 17, 17)
            setBackgroundColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 9
            layoutParams = params
        }
    }

    private fun addText(parent: LinearLayout, value: String, size: Float, color: Int, bold: Boolean) {
        val view = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 2, 0, 5)
        }
        parent.addView(view)
    }
}
