package com.maximintegrated.maximsensorsapp

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.bluetooth.devicelist.OnBluetoothDeviceClickListener
import com.maximintegrated.hsp.*
import com.maximintegrated.hsp.protocol.HspCommand
import com.maximintegrated.hsp.protocol.SetConfigurationCommand
import com.maximintegrated.hsp.protocol.Status
import com.maximintegrated.maximsensorsapp.bpt.BptMainFragment
import com.maximintegrated.maximsensorsapp.exts.getCurrentFragment
import com.maximintegrated.maximsensorsapp.exts.replaceFragment
import com.maximintegrated.maximsensorsapp.profile.User
import com.maximintegrated.maximsensorsapp.profile.UserViewModel
import com.maximintegrated.maximsensorsapp.profile.UserViewModelFactory
import com.maximintegrated.maximsensorsapp.service.ForegroundService
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import timber.log.Timber.d

class MainActivity : AppCompatActivity(), OnBluetoothDeviceClickListener {
    private lateinit var bluetoothDevice: BluetoothDevice

    private lateinit var hspViewModel: HspViewModel

    private lateinit var userViewModel: UserViewModel

    private var param1: ByteArray? = null

    private var param2: ByteArray? = null

    companion object {
        private const val KEY_BLUETOOTH_DEVICE = "com.maximintegrated.hsp.BLUETOOTH_DEVICE"

        fun start(context: Context, bluetoothDevice: BluetoothDevice) {
            context.startActivity(
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(KEY_BLUETOOTH_DEVICE, bluetoothDevice)
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        appVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        bluetoothDevice = intent.getParcelableExtra(KEY_BLUETOOTH_DEVICE)!!

        userViewModel = ViewModelProviders.of(this, UserViewModelFactory(
            (application as MaximSensorsApp).userDao
        )).get(UserViewModel::class.java)

        hspViewModel = ViewModelProviders.of(this).get(HspViewModel::class.java)
        hspViewModel.connect(bluetoothDevice)

        hspViewModel.isDeviceSupported
            .observe(this) {
                hspViewModel.sendCommand(HspCommand.fromText("get_device_info"))
            }

        hspViewModel.commandResponse
            .observe(this) { response ->
                if (response.command.name == HspCommand.COMMAND_GET_DEVICE_INFO && response.parameters.size > 1) {
                    val version = response.parameters[1].value
                    hspViewModel.deviceModel = when (version.split(".")[0].toIntOrNull() ?: 0) {
                        in 10..19 -> ME11A
                        in 20..29 -> ME11B
                        in 30..39 -> ME11C
                        in 40..49 -> ME11D
                        in 50..59 -> ME15  //MRD104
                        else -> UNDEFINED
                    }

                    if (hspViewModel.deviceModel == ME11D) {
                        showBptItems()
                    } else {
                        showMenuItems(arrayListOf("sensors"), arrayListOf("algoos"))
                        hspViewModel.sendCommand(HspCommand.fromText("set_cfg lcd time ${System.currentTimeMillis() / 1000}"))
                        hspViewModel.sendCommand(HspCommand.fromText("get_cfg sh_dhparams"))
                    }
                    serverVersion.text =
                        getString(R.string.server_version, response.parameters[0].value)
                    hubVersion.text = getString(R.string.hub_version, version)
                } else if (response.command.name == HspCommand.COMMAND_GET_CFG && response.parameters.isNotEmpty()) {
                    if (response.command.parameters[0].value == "sh_dhparams") {
                        val auth =
                            MaximAlgorithms.getAuthInitials(response.parameters[0].valueAsByteArray)
                        hspViewModel.sendCommand(HspCommand.fromText("set_cfg sh_dhlpublic ${auth.toHexString()}"))
                    } else if (response.command.parameters[0].value == "sh_dhrpublic") {
                        param1 = response.parameters[0].valueAsByteArray
                        hspViewModel.sendCommand(HspCommand.fromText("get_cfg sh_auth"))
                    } else if (response.command.parameters[0].value == "sh_auth") {
                        param2 = response.parameters[0].valueAsByteArray
                        var authenticated = -1
                        if (param1 != null && param2 != null) {
                            authenticated = MaximAlgorithms.authenticate(param1, param2)
                        }
                        if (authenticated == -1) {
                            showAuthenticationFailMessage()
                            d("authentication failed")
                        }
                        userViewModel.selectUserFromSettings(DeviceSettings.selectedUserId)
                    }
                } else if (response.command.name == HspCommand.COMMAND_SET_CFG) {
                    if (response.command.parameters[0].value == "sh_dhlpublic") {
                        hspViewModel.sendCommand(HspCommand.fromText("get_cfg sh_dhrpublic"))
                    }
                }
            }

        userViewModel.currentUser.observe(this){
            val user = it ?: User()
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "initialhr", user.initialHr.toString()))
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "personheight", user.heightInCm.toString()))
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "personweight", user.weightInKg.toString()))
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "personage", user.age.toString()))
            val gender = if(user.isMale) 0 else 1
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "persongender", gender.toString()))
        }

        d("Connected bluetooth device $bluetoothDevice")
    }

    private fun showMenuItems(deviceSensors: List<String>, firmwareAlgorithms: List<String>) {
        progressBar.isVisible = false
        replaceFragment(
            MainFragment.newInstance(
                deviceSensors.toTypedArray(),
                firmwareAlgorithms.toTypedArray()
            )
        )
    }

    private fun showBptItems() {
        progressBar.isVisible = false
        replaceFragment(BptMainFragment.newInstance())
    }

    private fun showAuthenticationFailMessage() {
        Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val fragment = (getCurrentFragment() as? IOnBackPressed)

        if (fragment != null) {
            fragment.onBackPressed().let {
                if (it) {
                    showStopMonitoringDialog()
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            if (getCurrentFragment() == null || getCurrentFragment() as? LandingPage != null) {
                startActivity(
                    Intent(this, ScannerActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun showStopMonitoringDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Stop Monitoring")
        alertDialog.setMessage("Are you sure you want to stop monitoring ?")
            .setPositiveButton("OK") { dialog, which ->
                (getCurrentFragment() as? IOnBackPressed)?.onStopMonitoring()
                dialog.dismiss()
                super.onBackPressed()
            }.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        val fragment = (getCurrentFragment() as? OnBluetoothDeviceClickListener)
        fragment?.onBluetoothDeviceClicked(bluetoothDevice)
    }

    override fun onStop() {
        super.onStop()
        val intent = Intent(this, ForegroundService::class.java)
        stopService(intent)
    }
}
