package pansong291.piano.wizard

import android.app.Application
import pansong291.piano.wizard.toast.Toaster

class PianoWizardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 Toaster 框架
        Toaster.initialize(this)
    }
}
