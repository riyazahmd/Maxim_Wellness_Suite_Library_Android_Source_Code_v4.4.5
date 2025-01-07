package com.maximintegrated.maximsensorsapp.bpt

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.bluetooth.ble.BleScannerDialog
import com.maximintegrated.bluetooth.devicelist.OnBluetoothDeviceClickListener
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.hsp.ME11D
import com.maximintegrated.hsp.ME15
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.addFragment
import com.maximintegrated.qardio.QardioViewModel
import kotlinx.android.synthetic.main.include_app_bar.*
import kotlinx.android.synthetic.main.include_bpt_main_fragment_content.*
import timber.log.Timber

class BptMainFragment : Fragment(), IOnBackPressed, OnBluetoothDeviceClickListener {

    companion object {
        fun newInstance(): BptMainFragment {
            return BptMainFragment()
        }

        const val DEVICE_NAME_PREFIX_QARDIO = "QardioARM"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_bpt_main, container, false)

    private lateinit var hspViewModel: HspViewModel

    private lateinit var bptViewModel: BptViewModel

    private lateinit var qardioViewModel: QardioViewModel

    private var bleScannerDialog: BleScannerDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)

        bptViewModel = ViewModelProviders.of(requireActivity()).get(BptViewModel::class.java)

        hspViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                toolbar.connectionInfo = if (hspViewModel.bluetoothDevice != null) {
                    BleConnectionInfo(connectionState, device?.name, device?.address)
                } else {
                    null
                }
            }

        toolbar.pageTitle = getString(R.string.bp_trending)

        bptViewModel.historyDataList.observe(this) {
            if (it == null) {
                calibrationMenuItemView.isEnabled = false
                measureBpMenuItemView.isEnabled = false
                bpHistoryMenuItemView.isEnabled = false
            } else {
                calibrationMenuItemView.isEnabled = true
                measureBpMenuItemView.isEnabled = true
                bpHistoryMenuItemView.isEnabled = true
            }
        }

        bptViewModel.refreshUserData()

        when (hspViewModel.deviceModel) {
            ME11D -> {
                bptUserGroup.isVisible = true
                bpTutorialMenuItemView.isVisible = true

                bptViewModel.userList.observe(this) {
                    setupAdapter(it)
                    if (BptSettings.currentUser != "") {
                        val index = BptSettings.users.indexOf(BptSettings.currentUser)
                        if (index == -1) {
                            BptSettings.currentUser = ""
                        } else {
                            userSpinner.setSelection(index + 1)
                        }
                    }
                }

                userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position == 0) {
                            BptSettings.currentUser = ""
                        } else {
                            BptSettings.currentUser =
                                bptViewModel.userList.value?.get(position) ?: ""
                        }
                        bptViewModel.refreshUserData()
                    }
                }

                newUserButton.setOnClickListener {
                    val name = newUserEditText.text.toString().trim()
                    if (name == "") {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.username_empty_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        BptSettings.currentUser = name
                        bptViewModel.addNewUser(name)
                        newUserEditText.setText("")
                        newUserEditText.onEditorAction(EditorInfo.IME_ACTION_DONE)
                        bptViewModel.refreshUserData()
                    }
                }
            }

            ME15 -> {
                bptUserGroup.isVisible = false
                bpTutorialMenuItemView.isVisible = false
            }
        }

        calibrationMenuItemView.setOnClickListener {
            if (BptSettings.currentUser == "") {
                showUserNotSelectedError()
            } else {
                val lastCalibration = bptViewModel.calibrationDataList.value?.lastOrNull()
                if (lastCalibration != null && !lastCalibration.isExpired()) {
                    val validCalibrations = bptViewModel.getValidCalibrationList()
                    var text = ""
                    for (cal in validCalibrations) {
                        text += "• ${USER_TIMESTAMP_FORMAT.format(cal.getDate())}  ${cal.sbp}/${cal.dbp}\n"
                    }
                    val message = if (validCalibrations.size < MAX_NUMBER_OF_CALIBRATION) {
                        getString(
                            R.string.valid_calibration_warning_not_max,
                            validCalibrations.size,
                            text
                        )
                    } else {
                        getString(
                            R.string.valid_calibration_warning_max,
                            validCalibrations.size,
                            text
                        )
                    }

                    val c = requireContext()
                    showAlertDialog(
                        c,
                        c.getString(R.string.warning),
                        message,
                        c.getString(R.string.yes)
                    ) {
                        requireActivity().addFragment(BptCalibrationFragment.newInstance())
                    }
                } else {
                    requireActivity().addFragment(BptCalibrationFragment.newInstance())
                }
            }
        }

        measureBpMenuItemView.setOnClickListener {
            if (BptSettings.currentUser == "") {
                showUserNotSelectedError()
            } else {
                val lastCalibration = bptViewModel.calibrationDataList.value?.lastOrNull()
                when {
                    lastCalibration == null -> {
                        val c = requireContext()
                        showAlertDialog(
                            c,
                            c.getString(R.string.warning),
                            c.getString(R.string.no_calibration_warning),
                            c.getString(R.string.ok)
                        ) {
                            requireActivity().addFragment(BptCalibrationFragment.newInstance())
                        }
                    }
                    lastCalibration.isExpired() -> {
                        val c = requireContext()
                        val date = USER_TIMESTAMP_FORMAT.format(lastCalibration.getDate())
                        showAlertDialog(
                            c,
                            c.getString(R.string.warning),
                            c.getString(R.string.expired_calibration_warning, date),
                            c.getString(R.string.ok)
                        ) {
                            requireActivity().addFragment(BptCalibrationFragment.newInstance())
                        }
                    }
                    else -> {
                        val validCalibrations = bptViewModel.getValidCalibrationList()
                        if (validCalibrations.size < SUGGESTED_NUMBER_OF_CALIBRATION) {
                            val c = requireContext()
                            showAlertDialog(
                                c,
                                c.getString(R.string.warning),
                                c.getString(
                                    R.string.less_calibration_warning,
                                    validCalibrations.size,
                                    SUGGESTED_NUMBER_OF_CALIBRATION
                                ),
                                c.getString(R.string.yes)
                            ) {
                                requireActivity().addFragment(BptMeasurementFragment.newInstance())
                            }
                        } else {
                            requireActivity().addFragment(BptMeasurementFragment.newInstance())
                        }
                    }
                }
            }
        }

        bpHistoryMenuItemView.setOnClickListener {
            if (BptSettings.currentUser == "") {
                showUserNotSelectedError()
            } else {
                requireActivity().addFragment(BptHistoryFragment.newInstance())
            }
        }

        bpTutorialMenuItemView.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    requireContext(),
                    BpTutorialActivity::class.java
                )
            )
        }

        setupReferenceDeviceView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }


    private fun setupAdapter(list: List<String>?) {
        ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            list ?: emptyList()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userSpinner.adapter = adapter
        }
    }

    private fun showUserNotSelectedError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.user_not_selected_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onBackPressed(): Boolean {
        if (hspViewModel.deviceModel == ME11D) {
            startActivity(
                Intent(requireActivity(), ScannerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            return false
        } else {
            return bptViewModel.isMonitoring.value ?: false
        }
    }

    override fun onStopMonitoring() {
        return
    }

    private fun setupReferenceDeviceView() {
        qardioViewModel = ViewModelProviders.of(requireActivity()).get(QardioViewModel::class.java)

        qardioViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                qardioViewMainFragment.bleConnectionInfo =
                    if (qardioViewModel.bluetoothDevice != null) {
                        BleConnectionInfo(connectionState, device?.name, device?.address)
                    } else {
                        null
                    }
            }

        qardioViewModel.batteryLevel
            .observe(this) { batteryLevel ->
                qardioViewMainFragment.bleConnectionInfo =
                    qardioViewMainFragment.bleConnectionInfo?.copy(
                        batteryLevel = batteryLevel
                    )
                Timber.d("Qardio Battery Level %d", batteryLevel)
            }

        qardioViewModel.isDeviceSupported
            .observe(this) {
                qardioViewModel.readBatteryLevel()
            }

        qardioViewMainFragment.onSearchButtonClick {
            showBleScannerDialog(R.string.bpt_ref_device, arrayListOf(DEVICE_NAME_PREFIX_QARDIO))
        }

        qardioViewMainFragment.onConnectButtonClick {
            qardioViewModel.reconnect()
        }

        qardioViewMainFragment.onDisconnectClick {
            qardioViewModel.disconnect()
        }

        qardioViewMainFragment.onChangeDeviceClick {
            qardioViewModel.disconnect()
            qardioViewMainFragment.bleConnectionInfo = null
            showBleScannerDialog(R.string.bpt_ref_device, arrayListOf(DEVICE_NAME_PREFIX_QARDIO))
        }

        qardioViewMainFragment.setOnlyDevice()
    }

    private fun showBleScannerDialog(@StringRes titleRes: Int, deviceNamePrefix: ArrayList<String>? = null) {
        bleScannerDialog = BleScannerDialog.newInstance(getString(titleRes), deviceNamePrefix)
        bleScannerDialog?.setTargetFragment(this, 1437)
        fragmentManager?.let { bleScannerDialog?.show(it, "BleScannerDialog") }
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        val deviceName = bluetoothDevice.name ?: ""
        when {
            deviceName.startsWith("", false) -> qardioViewModel.connect(
                bluetoothDevice
            )
        }
        bleScannerDialog?.dismiss()
    }
}