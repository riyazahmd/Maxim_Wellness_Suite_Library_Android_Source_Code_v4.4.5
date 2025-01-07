package com.maximintegrated.bluetooth.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProviders
import com.maximintegrated.bluetooth.R
import com.maximintegrated.bluetooth.common.BluetoothScannerDelegate
import com.maximintegrated.bluetooth.devicelist.OnBluetoothDeviceClickListener
import com.maximintegrated.bluetooth.extension.hasPermission
import com.maximintegrated.bluetooth.extension.hasPermissions
import com.maximintegrated.bluetooth.livedata.BleAvailableDevicesLiveData
import com.maximintegrated.bluetooth.util.verifyPermissions
import kotlinx.android.synthetic.main.activity_bluetooth_scanner.*
import timber.log.Timber

open class BleScannerActivity : AppCompatActivity(), OnBluetoothDeviceClickListener {

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
        const val REQUEST_STORAGE_PERMISSION = 2
        const val REQUEST_DEFAULT_LOCATION = 3
        private const val DEVICE_NAME_PREFIX_HSP = "HSP"
        private const val DEVICE_NAME_PREFIX_MAX30101 = "MAX30101"
        private const val DEVICE_NAME_PREFIX_MRD105 = "MRD105"
        private const val DEVICE_NAME_PREFIX_MRD104 = "MAXREFDES104"

        fun start(context: Context) {
            val intent = Intent(context, BleScannerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var scannerDelegate: BluetoothScannerDelegate
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scanner)
        scannerDelegate = BluetoothScannerDelegate(
            this,
            ViewModelProviders.of(this).get(BleScannerViewModel::class.java),
            arrayListOf(
                DEVICE_NAME_PREFIX_HSP,
                DEVICE_NAME_PREFIX_MAX30101,
                DEVICE_NAME_PREFIX_MRD105,
                DEVICE_NAME_PREFIX_MRD104
            )
        ).apply {
            deviceClickListener = this@BleScannerActivity
            scanStateChangeHandler = this@BleScannerActivity::invalidateOptionsMenu
        }

        with(devicesRecyclerView) {
            setHasFixedSize(true)
            adapter = scannerDelegate.deviceListAdapter
        }

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        var saveFileLocation = sharedPreferences.getString(getString(R.string.save_location_key), "")
        var locationExists: Boolean = false

        if (saveFileLocation!!.isNotEmpty()){
            locationExists = DocumentFile.fromTreeUri(this, Uri.parse(saveFileLocation))!!.exists()
            if (!locationExists){
                saveFileLocation = ""
                with(sharedPreferences.edit()){
                    putString(getString(R.string.save_location_key), "")
                    apply()
                }
            }
        }

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            scannerDelegate.showLocationPermissionMessage(View.OnClickListener {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
                )
            })
        } else if(saveFileLocation.isEmpty() || !locationExists){
            scannerDelegate.showSetDefaultLocationMessage(View.OnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, REQUEST_DEFAULT_LOCATION)
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION && verifyPermissions(grantResults)) {
            // permission was granted
            scannerDelegate.hideLocationPermissionMessage()
            BleAvailableDevicesLiveData.startScan(this)
        }
        if (requestCode == REQUEST_STORAGE_PERMISSION && verifyPermissions(grantResults)) {
            // permission was granted
            scannerDelegate.hideStoragePermissionMessage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DEFAULT_LOCATION && resultCode == RESULT_OK){
            data?.data?.also { uri ->
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                with(sharedPreferences.edit()){
                    putString(getString(R.string.save_location_key), uri.toString())
                    apply()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_bluetooth_scanner, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_scan_start).isVisible = scannerDelegate.isScanStartVisible
        menu.findItem(R.id.action_scan_stop).isVisible = scannerDelegate.isScanStopVisible
        if (!scannerDelegate.showingAnyErrorModel) {
            scannerWarningCardView.visibility = View.VISIBLE
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_scan_start -> {
            BleAvailableDevicesLiveData.startScan(this)
            true
        }
        R.id.action_scan_stop -> {
            BleAvailableDevicesLiveData.stopScan(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        // will be overridden
    }
}
