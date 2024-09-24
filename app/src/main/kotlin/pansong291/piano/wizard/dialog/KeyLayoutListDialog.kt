package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.contents.DialogRadioListContent
import pansong291.piano.wizard.entity.KeyLayout

class KeyLayoutListDialog(
    application: Application,
    data: List<KeyLayout>,
    default: Int
) : BaseDialog(application) {
    var onAction: (index: Int, actionId: Int) -> Unit = { _, _ -> }
    private val setSpecialActionEnabled: (b: Boolean) -> Unit
    private val adapter: DialogRadioListContent.Adapter =
        DialogRadioListContent.loadIn(this, data.map { it.name }, default)

    init {
        setTitle(R.string.select_layout)
        val actions = View.inflate(
            application,
            R.layout.dialog_actions_key_layout,
            findActionsWrapper()
        )
        // 操作区：保存、新建、删除、重命名、确定按钮
        val btnSave = actions.findViewById<AppCompatImageButton>(R.id.btn_save)
        val btnCreate = actions.findViewById<AppCompatImageButton>(R.id.btn_create)
        val btnDelete = actions.findViewById<AppCompatImageButton>(R.id.btn_delete)
        val btnRename = actions.findViewById<AppCompatImageButton>(R.id.btn_rename)
        val btnOk = actions.findViewById<AppCompatImageButton>(android.R.id.primary)
        View.OnClickListener {
            onAction.invoke(adapter.getSelectedPosition(), it.id)
        }.also {
            btnSave.setOnClickListener(it)
            btnCreate.setOnClickListener(it)
            btnDelete.setOnClickListener(it)
            btnRename.setOnClickListener(it)
            btnOk.setOnClickListener(it)
        }
        setSpecialActionEnabled = {
            btnDelete.isEnabled = it
            btnRename.isEnabled = it
            btnOk.isEnabled = it
            val alpha = if (it) 1f else .2f
            btnDelete.alpha = alpha
            btnRename.alpha = alpha
            btnOk.alpha = alpha
        }
        setSpecialActionEnabled(default >= 0 && default < data.size)
        adapter.onItemSelected = {
            setSpecialActionEnabled(true)
        }
    }

    fun reloadData(data: List<KeyLayout>, selected: Int?) {
        selected?.let {
            setSpecialActionEnabled(selected >= 0 && selected < data.size)
        }
        adapter.reload(data.map { it.name }, selected)
    }
}
