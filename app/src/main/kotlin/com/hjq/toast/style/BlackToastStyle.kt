package com.hjq.toast.style

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import pansong291.piano.wizard.utils.ViewUtil.dp
import pansong291.piano.wizard.utils.ViewUtil.dpInt

object BlackToastStyle {
    fun createView(context: Context): View {
        val textView = TextView(context)
        textView.id = android.R.id.message
        textView.gravity = Gravity.CENTER
        textView.setTextColor(0xffffffff.toInt())
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        val horizontalPadding = 24.dpInt()
        val verticalPadding = 16.dpInt()

        // 适配布局反方向特性
        textView.setPaddingRelative(
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            verticalPadding
        )

        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val backgroundDrawable = getBackgroundDrawable(context)
        // 设置背景
        textView.background = backgroundDrawable

        // 设置 Z 轴阴影
        textView.z = 3.dp()

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val wp = 48.dpInt()
            setPadding(0, wp, 0, wp)
            addView(textView)
        }
    }

    private fun getBackgroundDrawable(context: Context): Drawable {
        val drawable = GradientDrawable()
        // 设置颜色
        drawable.setColor(0xff000000.toInt())
        // 设置圆角
        drawable.cornerRadius = 10.dp()
        return drawable
    }
}
