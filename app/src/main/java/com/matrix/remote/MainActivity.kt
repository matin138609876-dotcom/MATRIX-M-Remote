package com.matrix.remote

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private val red = Color.rgb(235, 25, 45)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 190, 200)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(15, 5, 10)
        window.navigationBarColor = Color.BLACK

        val root = FrameLayout(this)

        val background = ImageView(this).apply {
            setImageResource(R.drawable.matrix_wallpaper)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        root.addView(background, FrameLayout.LayoutParams(-1, -1))

        val overlay = View(this).apply {
            setBackgroundColor(Color.argb(155, 5, 5, 10))
        }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(30))
        }

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
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(subtitle)

        content.addView(space(28))

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(18), dp(16), dp(18))
            setBackground(panel())
        }

        val statusTitle = TextView(this).apply {
            text = "وضعیت سیستم"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER
        }
        status.addView(statusTitle)

        val statusText = TextView(this).apply {
            text = "● سیستم آماده به کار است"
            textSize = 14f
            setTextColor(Color.rgb(100, 230, 140))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        status.addView(statusText)

        val deviceCount = TextView(this).apply {
            text = "دستگاه‌های متصل: ۰"
            textSize = 15f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }
        status.addView(deviceCount)

        content.addView(status, matchWrap())

        content.addView(space(24))

        val section = TextView(this).apply {
            text = "مدیریت دستگاه‌ها"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER
        }
        content.addView(section)

        content.addView(space(14))

        addButton(content, "＋  اتصال گوشی جدید")
        addButton(content, "▣  دستگاه‌های من")
        addButton(content, "⌖  کنترل صفحه گوشی")
        addButton(content, "⇄  انتقال فایل")
        addButton(content, "⚙  تنظیمات برنامه")

        content.addView(space(20))

        val footer = TextView(this).apply {
            text = "MATRIX M  •  VERSION 1.0"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        content.addView(footer)

        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        setContentView(root)
    }

    private fun addButton(parent: LinearLayout, label: String) {
        val button = TextView(this).apply {
            text = label
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(dp(12), dp(17), dp(12), dp(17))
            background = GradientDrawable().apply {
                setColor(Color.argb(220, 12, 10, 15))
                setCornerRadius(dp(12).toFloat())
                setStroke(dp(2), red)
            }
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(12)
        parent.addView(button, params)
    }

    private fun panel(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.argb(215, 10, 8, 14))
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
}
