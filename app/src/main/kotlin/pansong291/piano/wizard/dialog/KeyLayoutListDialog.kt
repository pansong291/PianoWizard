package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.View
import android.widget.Button
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.contents.DialogRadioListContent
import pansong291.piano.wizard.entity.KeyLayout

class KeyLayoutListDialog(
    application: Application,
    data: List<KeyLayout>,
    default: Int? = null
) : BaseDialog(application) {
    private var listener: OnActionListener? = null
    private val adapter: DialogRadioListContent.Adapter

    init {
        setOutsideCloseable(true)
        adapter = DialogRadioListContent.loadIn(this, data.map { it.name }, default)

        val actions = View.inflate(
            application,
            R.layout.dialog_actions_key_layout,
            getActionsWrapper()
        )
        // 操作区：新建、删除、重命名、确定按钮
        val btnCreate = actions.findViewById<Button>(R.id.btn_create)
        val btnDelete = actions.findViewById<Button>(R.id.btn_delete)
        val btnRename = actions.findViewById<Button>(R.id.btn_rename)
        val btnOk = actions.findViewById<Button>(android.R.id.primary)
        View.OnClickListener {
            listener?.onAction(adapter.getSelectedPosition(), it.id)
        }.also {
            btnCreate.setOnClickListener(it)
            btnDelete.setOnClickListener(it)
            btnRename.setOnClickListener(it)
            btnOk.setOnClickListener(it)
        }
    }

    fun reloadData(data: List<KeyLayout>, default: Int?) {
        adapter.reload(data.map { it.name }, default)
    }

    fun setOnActionListener(l: OnActionListener) {
        listener = l
    }

    fun interface OnActionListener {
        fun onAction(index: Int, actionId: Int)
    }
}
