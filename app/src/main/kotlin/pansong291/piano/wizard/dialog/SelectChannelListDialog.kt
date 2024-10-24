package pansong291.piano.wizard.dialog

import android.content.Context
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.actions.DialogCheckConfirmActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogCheckboxListContent

class SelectChannelListDialog(
    context: Context,
    data: List<Int>
) : BaseDialog(context) {
    var onConfirmed: ((selected: Set<Int>, merge: Boolean) -> Unit)? = null
    private val adapter: DialogCheckboxListContent.Adapter =
        DialogCheckboxListContent.loadIn(this, data.map { "Channel($it)" }, setOf())

    init {
        setIcon(R.drawable.outline_checklist_32)
        setTitle(R.string.select_channel)
        DialogCheckConfirmActions.loadIn(this) { cb, ok, cancel ->
            cb.setText(R.string.merge)
            ok.setOnClickListener {
                onConfirmed?.also {
                    it(adapter.getSelectedPositions().mapNotNullTo(mutableSetOf()) {
                        data.getOrNull(it)
                    }, cb.isChecked)
                }
            }
        }
    }
}
