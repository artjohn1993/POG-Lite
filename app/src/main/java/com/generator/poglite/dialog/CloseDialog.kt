package com.generator.poglite.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.Button
import com.generator.poglite.R

class CloseDialog {

    fun showDialog(activity: Activity) {
        var dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.close_app_dialog)

        var cancel = dialog.findViewById<Button>(R.id.cancelDialog)
        var okay = dialog.findViewById<Button>(R.id.closeDialog)

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        okay.setOnClickListener {
            activity.finish()
        }

        dialog.show()
    }
}