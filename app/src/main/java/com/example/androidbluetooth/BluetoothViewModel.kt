package com.example.androidbluetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<BluetoothDevice?>(null)
    val connectionState: StateFlow<BluetoothDevice?> = _connectionState.asStateFlow()

    init {
        bluetoothManager.registerDiscoveryCallback(object : BluetoothManager.DiscoveryCallback {
            override fun onDeviceDiscovered(device: BluetoothDevice) {

            }

            override fun onDiscoveryFinished() {

            }

        })

        bluetoothManager.registerConnectionStateCallback(object : BluetoothManager.ConnectionStateCallback {
            override fun onDeviceConnected(device: BluetoothDevice) {
                _connectionState.value = device
            }

            override fun onDeviceDisconnected(device: BluetoothDevice) {
                _connectionState.value = null
            }
        })
    }

    private fun addDiscoveredDevice(device: BluetoothDevice) {
        _discoveredDevices.value = _discoveredDevices.value + listOf(device)
    }

    fun makeDeviceDiscoverable() {
        bluetoothManager.makeDiscoverable()
    }

    fun startDiscovery() {
        bluetoothManager.startDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
    }

}