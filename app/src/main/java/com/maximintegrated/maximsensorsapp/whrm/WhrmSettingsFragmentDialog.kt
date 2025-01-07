package com.maximintegrated.maximsensorsapp.whrm

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.WhrmSettings
import kotlinx.android.synthetic.main.dialog_whrm_settings.*
import kotlinx.android.synthetic.main.dialog_whrm_settings.view.*
import kotlin.math.max
import kotlin.math.min

class WhrmSettingsFragmentDialog : DialogFragment() {

    companion object {

        fun newInstance(): WhrmSettingsFragmentDialog {
            return WhrmSettingsFragmentDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val contentView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_whrm_settings, null)
        contentView.sampleModeCycleTimeEditText.setText((WhrmSettings.sampledModeTimeInterval / 1000).toString())
        contentView.hrConfidenceLevelEditText.setText((WhrmSettings.minConfidenceLevel).toString())
        contentView.hrExpirationThresholdEditText.setText((WhrmSettings.confidenceThresholdInSeconds).toString())

        val fragment = targetFragment as? WhrmFragment

        val settingsDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.whrm_settings)
            .setView(contentView)
            .setPositiveButton(R.string.save) { dialog, which ->
                val d = dialog as AlertDialog
                val timeInterval = max(
                    d.sampleModeCycleTimeEditText.text.toString().toLong() * 1000,
                    WhrmFragment.MIN_CYCLE_TIME_IN_MILLIS
                )
                if (timeInterval != WhrmSettings.sampledModeTimeInterval) {
                    WhrmSettings.sampledModeTimeInterval = timeInterval
                    fragment?.setupTimer()
                }
                val confidenceLevel = d.hrConfidenceLevelEditText.text.toString().toFloat().toInt()
                val threshold = d.hrExpirationThresholdEditText.text.toString().toFloat().toInt()
                WhrmSettings.minConfidenceLevel = min(max(0, confidenceLevel), 100)
                WhrmSettings.confidenceThresholdInSeconds = max(0, threshold)
            }.setNegativeButton(R.string.cancel) { dialog, which ->
                dialog.dismiss()
            }
            .create()

        settingsDialog.setCancelable(false)
        settingsDialog.setCanceledOnTouchOutside(false)

        return settingsDialog
    }
}