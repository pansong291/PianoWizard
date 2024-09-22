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

object BlackToastStyle {
    fun createView(context: Context): View {
        val textView = TextView(context)
        textView.id = android.R.id.message
        textView.gravity = Gravity.CENTER
        textView.setTextColor(-0x11000001)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize(context))

        val horizontalPadding = getHorizontalPadding(context)
        val verticalPadding = getVerticalPadding(context)

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
        textView.z = getTranslationZ(context)

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val wp = getWrapperPadding(context)
            setPadding(0, wp, 0, wp)
            addView(textView)
        }
    }

    private fun getWrapperPadding(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f, context.resources.displayMetrics
        ).toInt()
    }

    private fun getTextSize(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            14f, context.resources.displayMetrics
        )
    }

    private fun getHorizontalPadding(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            24f, context.resources.displayMetrics
        ).toInt()
    }

    private fun getVerticalPadding(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f, context.resources.displayMetrics
        ).toInt()
    }

    private fun getBackgroundDrawable(context: Context): Drawable {
        val drawable = GradientDrawable()
        // 设置颜色
        drawable.setColor(-0x4d000000)
        // 设置圆角
        drawable.cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f, context.resources.displayMetrics
        )
        return drawable
    }

    private fun getTranslationZ(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            context.resources.displayMetrics
        )
    }
}
