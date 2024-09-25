package pansong291.piano.wizard.toast.style

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import pansong291.piano.wizard.utils.ViewUtil

object BlackToastStyle {
    fun createView(context: Context): View {
        val textView = TextView(context)
        textView.id = android.R.id.message
        textView.gravity = Gravity.CENTER
        textView.setTextColor(-0x11000001)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        val horizontalPadding = ViewUtil.dpToPx(context, 24f).toInt()
        val verticalPadding = ViewUtil.dpToPx(context, 16f).toInt()

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
        textView.z = ViewUtil.dpToPx(context, 3f)

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val wp = ViewUtil.dpToPx(context, 48f).toInt()
            setPadding(0, wp, 0, wp)
            addView(textView)
        }
    }

    private fun getBackgroundDrawable(context: Context): Drawable {
        val drawable = GradientDrawable()
        // 设置颜色
        drawable.setColor(-0x4d000000)
        // 设置圆角
        drawable.cornerRadius = ViewUtil.dpToPx(context, 10f)
        return drawable
    }
}
