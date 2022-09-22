package com.generator.poglite.dialog

import android.R
import android.app.Activity
import android.app.Dialog
import android.view.Window

class UrlCheckerDialog(var activity: Activity) {
    var dialog = Dialog(activity, R.style.Theme_NoTitleBar_Fullscreen)

    init {
        dialog.window?.setBackgroundDrawableResource(com.generator.poglite.R.color.transparent_black)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(com.generator.poglite.R.layout.loading_url_checking)
    }

    fun show() {
        try {
            dialog.show()
        } catch (e: Exception) {
        }
    }

    fun hide() {
        try {
            dialog.dismiss()
        }catch (e: java.lang.Exception) {
            this.dismiss()
        }
    }

    fun dismiss() {
        dialog.dismiss()
    }
}