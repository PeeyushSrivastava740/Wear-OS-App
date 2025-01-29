package com.vc.myfirstwearapp.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothManager(private val context: Context, private val listener: BluetoothListener) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    val discoveredDevices = mutableListOf<BluetoothDevice>()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var connectedDevice: BluetoothDevice? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
    private val TAG = "BluetoothManager"

    interface BluetoothListener {
        fun onDataReceived(data: String)
        fun onDeviceConnected()
        fun onDeviceDisconnected()
        fun onScanStarted()
        fun onScanStopped()
        fun onError(message: String)
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        Log.d(TAG, "Found device: ${device.name} - ${device.address}")
                        if (!discoveredDevices.contains(device)) {
                            discoveredDevices.add(device)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    listener.onScanStopped()
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(receiver, filter)
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter!!.cancelDiscovery()
        }
        if (bluetoothAdapter?.startDiscovery() == true) {
            listener.onScanStarted()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            bluetoothSocket?.connect()
            connectedDevice = device
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            listener.onDeviceConnected()
            startListeningForData()
        } catch (e: IOException) {
            listener.onError("Error connecting to device: ${e.message}")
        }
    }

    private fun startListeningForData() {
        val buffer = ByteArray(1024)
        var bytes: Int
        while (bluetoothSocket?.isConnected == true) {
            try {
                bytes = inputStream?.read(buffer) ?: 0
                val data = String(buffer, 0, bytes)
                listener.onDataReceived(data)
            } catch (e: IOException) {
                listener.onError("Error reading data: ${e.message}")
                break
            }
        }
    }

    fun sendData(data: String) {
        try {
            outputStream?.write(data.toByteArray())
        } catch (e: IOException) {
            listener.onError("Error sending data: ${e.message}")
        }
    }

    fun close() {
        try {
            bluetoothSocket?.close()
            context.unregisterReceiver(receiver)
        } catch (e: IOException) {
            listener.onError("Error closing connection: ${e.message}")
        }
    }
}
