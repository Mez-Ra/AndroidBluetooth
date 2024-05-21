package com.example.androidbluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    private val bluetoothViewModel: BluetoothViewModel by viewModels()
    private val requestBluetoothPermissionCode = 101
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

    companion object {
        private const val REQUEST_CODE_BLUETOOTH_ADVERTISE = 101
    }

    private fun requestBluetoothAdvertisePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), REQUEST_CODE_BLUETOOTH_ADVERTISE)
        } else {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothAdvertisePermission()
        setContent {
            BluetoothScreen(bluetoothViewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        bluetoothManager.registerConnectionReceiver()
    }

    override fun onStop() {
        super.onStop()
        bluetoothManager.unregisterConnectionReceiver()
    }


    @Composable
    fun BluetoothScreen(bluetoothViewModel: BluetoothViewModel) {
        val discoveredDevices by bluetoothViewModel.discoveredDevices.collectAsState()
        val connectedDevice by bluetoothViewModel.connectionState.collectAsState()
        var discoverableMessage by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                bluetoothViewModel.makeDeviceDiscoverable()
                discoverableMessage = "Device is now discoverable"
            }) {
                Text("Make Discoverable")
            }

            if (discoverableMessage.isNotEmpty()) {
                Text(discoverableMessage)
            }
            Button(onClick = { bluetoothViewModel.startDiscovery() }) {
                Text("Start Discovery")
            }
            Log.d("Compose", "Device: ${connectedDevice?.name}")
            connectedDevice?.let {
                if (ActivityCompat.checkSelfPermission(
                        LocalContext.current,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                Text("Connected to: ${it.name}")
            } ?: Text("Not connected to any device")

            ListDiscoveredDevices(devices = discoveredDevices)
        }
    }

    @Composable
    fun ListDiscoveredDevices(devices: List<BluetoothDevice>) {
        Column {
            for(device in devices) {
                if(ActivityCompat.checkSelfPermission(
                    LocalContext.current,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                Text(text = "Device found: ${device.name} | ${device.address}")
            }
        }
    }

    private fun checkPermissionsAndStartDiscovery() {
        val requiredPermissions = mutableListOf<String>()

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if(requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), requestBluetoothPermissionCode)
        } else {
            bluetoothManager.startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == requestBluetoothPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            bluetoothManager.startDiscovery()
        }
    }

    @Composable
    fun BluetoothControls(bluetoothManager: BluetoothManager, onDiscoveredDevices: () -> Unit) {
        var bluetoothEnabled by remember { mutableStateOf(bluetoothManager.isBluetoothEnabled()) }
        var bluetoothSupported by remember { mutableStateOf(bluetoothManager.isBluetoothSupported()) }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Bluetooth Supported: $bluetoothSupported")
            Text(text = "Bluetooth Enabled: $bluetoothEnabled")
            Button(onClick = {
                bluetoothManager.enableBluetooth()
                bluetoothEnabled = bluetoothManager.isBluetoothEnabled()
            }) {
                Text("Enabled Bluetooth")
            }
            Button(onClick = onDiscoveredDevices) {
                Text("Start Discovery")
            }
        }


    }



}