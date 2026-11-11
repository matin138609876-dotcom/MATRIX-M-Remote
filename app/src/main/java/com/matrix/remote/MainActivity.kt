package com.matrix.remote

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }

        val title = TextView(this).apply {
            text = "MATRIX M"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "REMOTE DEVICE CONNECTION"
            textSize = 14f
            setTextColor(Color.rgb(148, 163, 184))
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)

        setContentView(layout)
    }
}
