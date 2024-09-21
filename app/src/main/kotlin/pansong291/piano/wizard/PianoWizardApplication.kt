package pansong291.piano.wizard

import android.app.Application
import com.hjq.toast.Toaster

class PianoWizardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 Toaster 框架
        Toaster.init(this)
    }
}
