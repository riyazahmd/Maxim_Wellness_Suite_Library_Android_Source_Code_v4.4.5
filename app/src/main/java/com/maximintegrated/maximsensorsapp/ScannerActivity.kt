package com.maximintegrated.maximsensorsapp

import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import com.maximintegrated.bluetooth.ble.BleScannerActivity
import timber.log.Timber


class ScannerActivity : BleScannerActivity() {

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        Timber.d("Bluetooth device is clicked")
        MainActivity.start(this, bluetoothDevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    /**
     * Checks if a default save location is selected, if it is selected then create MaximSensorsApp
     * and MaximSleepQa folders if they do not exists.
     */
    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        val saveLocationString = sharedPreferences.getString(getString(R.string.save_location_key), "") //get default file save location, if available

        if (saveLocationString!!.isNotEmpty()){  //if a save location selected, initialize important directories
            val treeRoot = DocumentFile.fromTreeUri(applicationContext, Uri.parse(saveLocationString))!!
            MWA_OUTPUT_DIRECTORY = if (treeRoot.findFile("MaximSensorsApp") == null){
                treeRoot.createDirectory("MaximSensorsApp")!!
            } else{
                treeRoot.findFile("MaximSensorsApp")!!
            }

            SQA_OUTPUT_DIRECTORY = if (treeRoot.findFile("MaximSleepQa") == null){
                treeRoot.createDirectory("MaximSleepQa")!!
            } else{
                treeRoot.findFile("MaximSleepQa")!!
            }
        }
    }
}