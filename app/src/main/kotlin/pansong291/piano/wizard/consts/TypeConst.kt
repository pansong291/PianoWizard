package pansong291.piano.wizard.consts

import com.google.gson.reflect.TypeToken
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.SkyStudioSheet

object TypeConst {
    val listOfKeyLayout by lazy {
        object : TypeToken<List<KeyLayout>>() {}
    }

    val listOfSkyStudioSheet by lazy {
        object : TypeToken<List<SkyStudioSheet>>() {}
    }
}
