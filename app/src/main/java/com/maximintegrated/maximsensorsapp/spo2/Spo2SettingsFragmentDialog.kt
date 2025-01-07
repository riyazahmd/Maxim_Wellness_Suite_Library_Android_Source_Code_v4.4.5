package com.maximintegrated.maximsensorsapp.spo2

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.maximintegrated.maximsensorsapp.R

class Spo2SettingsFragmentDialog : DialogFragment() {

    companion object {

        fun newInstance(): Spo2SettingsFragmentDialog {
            return Spo2SettingsFragmentDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contentView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_spo2_settings, null)

        val settingsDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.spo2_settings)
            .setView(contentView)
            .setPositiveButton(R.string.save) { dialog, which ->
                val dialog = dialog as Dialog
            }.setNegativeButton(R.string.cancel) { dialog, which ->
                dialog.dismiss()
            }
            .create()

        settingsDialog.setCancelable(false)
        settingsDialog.setCanceledOnTouchOutside(false)

        return settingsDialog
    }
}