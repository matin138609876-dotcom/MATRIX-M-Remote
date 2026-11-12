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

    private lateinit var root: FrameLayout
    private lateinit var background: ImageView
    private lateinit var overlay: View
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(12, 5, 10)
        window.navigationBarColor = Color.BLACK

        root = FrameLayout(this)

        background = ImageView(this).apply {
            setImageResource(R.drawable.matrix_wallpaper)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }
        root.addView(background, FrameLayout.LayoutParams(-1, -1))

        overlay = View(this).apply {
            setBackgroundColor(Color.argb(105, 0, 0, 0))
        }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        buildHome()
        setContentView(root)
    }

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

        val status = TextView(this).apply {
            text = "●  سیستم آماده به کار است"
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
            contentDescription = "ورود به اتصال امن و ساخت QR"
            setOnClickListener {
                showFeature(
                    "اتصال امن",
                    "ساخت QR Code برای اتصال گوشی دوم",
                    "در این بخش QR اختصاصی اتصال ساخته خواهد شد."
                )
            }
        }
        val maskParams = LinearLayout.LayoutParams(-1, dp(190))
        content.addView(mask, maskParams)

        val hint = TextView(this).apply {
            text = "برای اتصال امن، نقاب را لمس کن"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(hint)
        content.addView(space(28))

        val section = TextView(this).apply {
            text = "قابلیت‌های MATRIX M"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        content.addView(section)
        content.addView(space(14))

        addFeature("📷", "دوربین", "مشاهده دوربین گوشی دوم")
        addFeature("▣", "نمایشگر", "اشتراک صفحه گوشی دوم")
        addFeature("📁", "انتقال فایل", "انتقال فایل با اجازه صاحب دستگاه")
        addFeature("⚙", "تنظیمات", "تنظیمات و مدیریت دسترسی‌ها")

        content.addView(space(22))

        val footer = TextView(this).apply {
            text = "MATRIX M  •  VERSION 1.0"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        content.addView(footer)
    }

    private fun addFeature(icon: String, title: String, description: String) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = transparentPanel()
            isClickable = true
            isFocusable = true
            setOnClickListener {
                showFeature(title, description, "این قابلیت در مراحل بعدی فعال می‌شود.")
            }
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

    private fun showFeature(title: String, subtitle: String, message: String) {
        content.removeAllViews()

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

    override fun onBackPressed() {
        buildHome()
    }
}
