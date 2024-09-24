package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.View
import android.widget.ImageButton
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.contents.DialogRadioListContent
import pansong291.piano.wizard.entity.KeyLayout

class KeyLayoutListDialog(
    application: Application,
    data: List<KeyLayout>,
    default: Int? = null
) : BaseDialog(application) {
    var onAction: OnActionListener? = null
    private val adapter: DialogRadioListContent.Adapter

    init {
        setOutsideCloseable(true)
        adapter = DialogRadioListContent.loadIn(this, data.map { it.name }, default)

        val actions = View.inflate(
            application,
            R.layout.dialog_actions_key_layout,
            findActionsWrapper()
        )
        // 操作区：保存、新建、删除、重命名、确定按钮
        val btnSave = actions.findViewById<ImageButton>(R.id.btn_save)
        val btnCreate = actions.findViewById<ImageButton>(R.id.btn_create)
        val btnDelete = actions.findViewById<ImageButton>(R.id.btn_delete)
        val btnRename = actions.findViewById<ImageButton>(R.id.btn_rename)
        val btnOk = actions.findViewById<ImageButton>(android.R.id.primary)
        View.OnClickListener {
            onAction?.onAction(adapter.getSelectedPosition(), it.id)
        }.also {
            btnSave.setOnClickListener(it)
            btnCreate.setOnClickListener(it)
            btnDelete.setOnClickListener(it)
            btnRename.setOnClickListener(it)
            btnOk.setOnClickListener(it)
        }
    }

    fun reloadData(data: List<KeyLayout>, selected: Int?) {
        adapter.reload(data.map { it.name }, selected)
    }

    fun interface OnActionListener {
        fun onAction(index: Int, actionId: Int)
    }
}
