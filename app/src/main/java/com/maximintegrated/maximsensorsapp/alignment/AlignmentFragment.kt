package com.maximintegrated.maximsensorsapp.alignment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.maximintegrated.maximsensorsapp.MWA_OUTPUT_DIRECTORY
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.align
import kotlinx.android.synthetic.main.fragment_alignment.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AlignmentFragment : Fragment() {

    companion object {
        fun newInstance() = AlignmentFragment()
    }

    /**
     * Request codes for selecting raw and reference file
     */
    private val REQUEST_RAW_FILE = 1
    private val REQUEST_REF_FILE = 2


    private var rawFile: DocumentFile? = null
        set(value) {
            field = value
            rawFileTextView.text = rawFile?.uri?.path ?: ""
            savedFile = null
        }

    private var refFile: DocumentFile? = null
        set(value) {
            field = value
            refFileTextView.text = refFile?.uri?.path ?: ""
            savedFile = null
        }

    private var savedFile: DocumentFile? = null
        set(value) {
            field = value
            savedTextView.text = value?.uri?.path ?: ""
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rawFileImageView.setOnClickListener {
            showFilesToImportRawFile()
        }
        refFileImageView.setOnClickListener {
            showFilesToImportRefFile()
        }
        refFileInfoImageView.setOnClickListener {
            showRefFileDialog()
        }
        alignButton.setOnClickListener {
            savedFile = null
            warningMessageView.visibility = View.GONE
            if (rawFile == null || refFile == null) return@setOnClickListener
            progressBar.visibility = View.VISIBLE
            doAsync {
                try {
                    val file = align(rawFile!!, refFile!!, requireContext())
                    uiThread {
                        savedFile = file
                        warningMessageView.text = getString(R.string.alignment_successful)
                        warningMessageView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_check,
                            0,
                            0,
                            0
                        )
                        warningMessageView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    uiThread {
                        warningMessageView.text = e.message
                        warningMessageView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_warning,
                            0,
                            0,
                            0
                        )
                        warningMessageView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Opens system file picker UI for selecting raw file by creating an intent with action
     * ACTION_OPEN_DOCUMENT. If the device runs Android Oreo or later, then it sets the default
     * location as MWA_OUTPUT_DIRECTORY.
     */
    private fun showFilesToImportRawFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MWA_OUTPUT_DIRECTORY.uri)
            }
        }
        startActivityForResult(intent, REQUEST_RAW_FILE)
    }

    /**
     * Opens system file picker UI for selecting reference file by creating an intent with action
     * ACTION_OPEN_DOCUMENT. If the device runs Android Oreo or later, then it sets the default
     * location as MWA_OUTPUT_DIRECTORY.
     */
    private fun showFilesToImportRefFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MWA_OUTPUT_DIRECTORY.uri)
            }
        }
        startActivityForResult(intent, REQUEST_REF_FILE)
    }

    private fun showRefFileDialog() {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_ref_file_info, null)
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setView(contentView)
        alertDialog.show()
    }

    /**
     * If user selects a file as raw or reference file, then it initializes the corresponding
     * variables based on the request code.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_RAW_FILE && resultCode == RESULT_OK){
            rawFile = DocumentFile.fromSingleUri(requireContext(), data?.data!!)
        } else if (requestCode == REQUEST_REF_FILE && resultCode == RESULT_OK){
            refFile = DocumentFile.fromSingleUri(requireContext(), data?.data!!)
        }
    }
}