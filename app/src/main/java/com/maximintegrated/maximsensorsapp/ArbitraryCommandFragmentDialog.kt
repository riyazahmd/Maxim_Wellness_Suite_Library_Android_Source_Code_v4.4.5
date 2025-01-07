package com.maximintegrated.maximsensorsapp

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.hsp.protocol.HspCommand
import kotlinx.android.synthetic.main.dialog_send_arbitrary_command.*
import kotlinx.android.synthetic.main.dialog_send_arbitrary_command.view.*

class ArbitraryCommandFragmentDialog : DialogFragment() {
    companion object {

        fun newInstance(): ArbitraryCommandFragmentDialog {
            return ArbitraryCommandFragmentDialog()
        }
    }

    private lateinit var hspViewModel: HspViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contentView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_send_arbitrary_command, null)


        val sendArbitraryCommandDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.send_arbitrary_command))
            .setView(contentView)
            .setPositiveButton(getString(R.string.send)) { dialog, which ->

            }.setNegativeButton(R.string.cancel) { dialog, which ->
                dialog.dismiss()
            }
            .create()

        sendArbitraryCommandDialog.setCancelable(false)
        sendArbitraryCommandDialog.setCanceledOnTouchOutside(false)

        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)

        hspViewModel.commandResponse
            .observe(this) { response ->
                if (contentView.editTextCommand.text.toString().contains(response.command.name)) {
                    contentView.textViewResponse.text = response.status.message
                    contentView.textViewResponse.setBackgroundColor(if (response.status.code == 0) Color.GREEN else Color.RED)
                }
            }

        return sendArbitraryCommandDialog
    }

    override fun onResume() {
        super.onResume()
        val d = this.dialog as AlertDialog

        val editText = d.editTextCommand
        val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener {
            val wantToCloseDialog = false

            hspViewModel.sendCommand(HspCommand.fromText(editText.text.toString()))
            if (wantToCloseDialog)
                d.dismiss()
        }
    }
}