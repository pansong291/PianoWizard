package pansong291.piano.wizard.entity

/**
 * ```json
 * [
 *   {
 *     "name": "test",
 *     "author": "",
 *     "arrangedBy": "",
 *     "transcribedBy": "",
 *     "permission": "",
 *     "bpm": 240,
 *     "bitsPerPage": 16,
 *     "pitchLevel": 0,
 *     "songNotes": [
 *       { "time": 250, "key": "1Key0" },
 *       { "time": 500, "key": "1Key1" },
 *       { "time": 1000, "key": "1Key2" },
 *       { "time": 1750, "key": "1Key3" },
 *       { "time": 2750, "key": "1Key4" },
 *       { "time": 3000, "key": "1Key5" },
 *       { "time": 3250, "key": "1Key6" },
 *       { "time": 3500, "key": "1Key7" },
 *       { "time": 3750, "key": "1Key8" },
 *       { "time": 4000, "key": "1Key9" },
 *       { "time": 4250, "key": "1Key10" },
 *       { "time": 4500, "key": "1Key11" },
 *       { "time": 4750, "key": "1Key12" },
 *       { "time": 5000, "key": "1Key13" },
 *       { "time": 5250, "key": "1Key14" }
 *     ]
 *   }
 * ]
 * ```
 */
class SkyStudioSheet {
    var name: String? = null
    var author: String? = null
    var arrangedBy: String? = null
    var transcribedBy: String? = null
    var permission: String? = null
    var bpm: Double? = null
    var bitsPerPage: Double? = null
    var pitchLevel: Double? = null
    var songNotes: List<SongNote>? = null

    class SongNote {
        var time: Double? = null
        var key: String? = null
    }
}
