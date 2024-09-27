package pansong291.piano.wizard.toast.config

import android.view.View
import android.widget.TextView

interface IToast {

    /**
     * 显示
     */
    fun show()

    /**
     * 取消
     */
    fun cancel()

    /**
     * 设置文本
     */
    fun setText(text: CharSequence?)

    /**
     * 设置显示时长
     */
    fun setDuration(duration: Int)

    /**
     * 获取显示时长
     */
    fun getDuration(): Int

    /**
     * 智能获取用于显示消息的 TextView
     */
    fun findMessageView(view: View): TextView {
        if (view is TextView) {
            if (view.getId() == View.NO_ID) {
                view.setId(android.R.id.message)
            } else require(view.getId() == android.R.id.message) {
                // 必须将 TextView 的 id 值设置成 android.R.id.message
                // 否则 Android 11 手机上在后台 toast.setText 的时候会出现报错
                // java.lang.RuntimeException: This Toast was not created with Toast.makeText()
                "You must set the ID value of TextView to android.R.id.message"
            }
            return view
        }

        val messageView = view.findViewById<View>(android.R.id.message)
        require(messageView is TextView) {
            // 如果设置的布局没有包含一个 TextView 则抛出异常，必须要包含一个 id 值为 message 的 TextView（xml 代码 android:id="@android:id/message"，java 代码 view.setId(android.R.id.message)）
            throw IllegalArgumentException("You must include a TextView with an ID value of message (xml code: android:id=\"@android:id/message\", java code: view.setId(android.R.id.message))")
        }
        return messageView
    }
}
