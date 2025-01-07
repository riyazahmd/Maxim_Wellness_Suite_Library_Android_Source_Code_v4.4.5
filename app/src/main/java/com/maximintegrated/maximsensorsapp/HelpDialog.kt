package com.maximintegrated.maximsensorsapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_help.view.*

class HelpDialog : DialogFragment() {

    private var text = ""
    private var title = ""

    companion object {
        fun newInstance(text: String, title: String): HelpDialog {
            val dialog = HelpDialog()
            dialog.text = text
            dialog.title = title
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contentView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_help, null)

        contentView.help_text_view.text = text

        val helpDialog = AlertDialog.Builder(requireContext())
            //.setTitle(title)
            .setView(contentView)
            .setPositiveButton(R.string.ok) { dialog, which ->
                dialog.dismiss()
            }
            .create()
        helpDialog.setCancelable(true)
        helpDialog.setCanceledOnTouchOutside(true)

        return helpDialog
    }
}