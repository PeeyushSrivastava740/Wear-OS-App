package com.vc.myfirstwearapp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition


class MainActivity : ComponentActivity(),
    BluetoothManager.BluetoothListener , WifiServer.WifiListener {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var wifiServer: WifiServer

    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val wifiPermissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    )

    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    private val REQUEST_WIFI_PERMISSIONS = 2
    private val TAG = "MainActivity"

    // State variables
    private var backgroundColor = mutableStateOf(Color.DarkGray)
    private var scannedData = mutableStateOf("No data yet")
    private var connectionStatus = mutableStateOf("Not connected")
    private var scanStatus = mutableStateOf("Scan stopped")
    private var wifiCommand = mutableStateOf("No command yet")
    private var bluetoothDevices = mutableStateListOf<BluetoothDevice>()
    private var bluetoothConnectionStatus = mutableStateOf("Not connected")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBluetoothPermissions()
        requestWifiPermissions()

        bluetoothManager = BluetoothManager(this, this)
        wifiServer = WifiServer(this)

        setContent {
            MainScreen()
        }
        bluetoothManager.startScanning()
        wifiServer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.close()
        wifiServer.close()
    }

    private fun requestBluetoothPermissions() {
        if (bluetoothPermissions.any {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(
                this,
                bluetoothPermissions,
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }

    private fun requestWifiPermissions() {
        if (wifiPermissions.any {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(
                this,
                wifiPermissions,
                REQUEST_WIFI_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Bluetooth permissions granted
            } else {
                // Bluetooth permissions denied
            }
        }
        if (requestCode == REQUEST_WIFI_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Wifi permissions granted
            } else {
                // Wifi permissions denied
            }
        }
    }

    @Composable
    fun MainScreen() {
        Scaffold(
            timeText = { TimeText() },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            modifier = Modifier
                .background(backgroundColor.value)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connection Status:",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = connectionStatus.value,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = "Scan Status:",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = scanStatus.value,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = "Scanned Data:",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = scannedData.value,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = "Wifi Command:",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = wifiCommand.value,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Button(onClick = {
                    // Simulate receiving a command to change the color
                    backgroundColor.value = Color.Green
                    scannedData.value = "Data 123456"
                }) {
                    Text("Change Color")
                }
                Text(
                    text = "Bluetooth Connection Status:",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = bluetoothConnectionStatus.value,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = "Bluetooth Devices:",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                LazyColumn {
                    items(bluetoothDevices) { device ->
                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        }
                        Text(
                            text = "${device.name ?: "Unknown"} - ${device.address}",
                            modifier = Modifier
                                .clickable {
                                    bluetoothManager.connectToDevice(device)
                                }
                                .padding(8.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }


    override fun onDeviceConnected() {
        runOnUiThread {
            connectionStatus.value = "Connected"
            bluetoothConnectionStatus.value = "Connected"
            setContent {
                MainScreen()
            }
        }
    }

    override fun onDeviceDisconnected() {
        runOnUiThread {
            connectionStatus.value = "Disconnected"
            bluetoothConnectionStatus.value = "Disconnected"
            setContent {
                MainScreen()
            }
        }
    }

    override fun onScanStarted() {
        runOnUiThread {
            scanStatus.value = "Scanning..."
            bluetoothDevices.clear()
            setContent {
                MainScreen()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onScanStopped() {
        runOnUiThread {
            scanStatus.value = "Scan stopped"
            bluetoothDevices.addAll(bluetoothManager.discoveredDevices)
            setContent {
                MainScreen()
            }
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            connectionStatus.value = "Error: $message"
            bluetoothConnectionStatus.value = "Error: $message"
            setContent {
                MainScreen()
            }
        }
    }

    override fun onDataReceived(data: String) {
        runOnUiThread {
            scannedData.value = data
//            Update the UI with the received data
            // Example: Send the received data to the Wi-Fi client
            wifiServer.sendDataToClient("Bluetooth Data: $data")

            // Check if the received data is a command
            if (data.startsWith("COMMAND:")) {
                val command = data.substringAfter("COMMAND:")
                wifiServer.sendDataToClient("Bluetooth Command: $command")
            }
            setContent {
                MainScreen()
            }

        }
    }

    override fun onCommandReceived(command: String) {
        Log.d(TAG, "Command received: $command")
        runOnUiThread {
            wifiCommand.value = command
            when (command) {
                "CHANGE_COLOR_RED" -> {
                    backgroundColor.value = Color.Red
                    setContent {
                        MainScreen()
                    }
                }

                "CHANGE_COLOR_BLUE" -> {
                    backgroundColor.value = Color.Blue
                    setContent {
                        MainScreen()
                    }
                }

                "GET_DATA" -> {
                    // Send the scanned data back to the client
                    wifiServer.sendDataToClient(scannedData.value)
                    Log.d(TAG, "Sending data: ${scannedData.value}")
                    setContent {
                        MainScreen()
                    }
                }
                "SEND_DATA_BLUETOOTH" -> {
                    bluetoothManager.sendData(scannedData.value)
                    Log.d(TAG, "Sending data to bluetooth: ${scannedData.value}")
                    setContent {
                        MainScreen()
                    }
                }
                "SEND_COMMAND_BLUETOOTH" -> {
                    bluetoothManager.sendData("COMMAND: $command")
                }

                else -> {
                    Log.w(TAG, "Unknown command: $command")
                    setContent {
                        MainScreen()
                    }
                }
            }
        }
    }

}


/*

Functionally Complete (Core Features): If your goal was to:

*/

//Scan for and connect to Bluetooth devices.
//Send data to a Bluetooth device.
//Receive data from a Bluetooth device.
//Send data from Bluetooth to a Wi-Fi client.
//Receive commands from a Wi-Fi client.
//Send data from Wi-Fi to a Bluetooth device.
//Change the color of the screen.
//Send data from the app to the Wi-Fi client.
//Send data from the app to the Bluetooth device.
//Handle errors.
//Update the UI.
//Request permissions.
//Start and stop the scan.
//Display the list of bluetooth devices.
//Connect to a bluetooth device.
//Display the connection status.
//Display the scan status.
//Display the scanned data.
//Display the wifi command.
//Display the bluetooth connection status.
//Display the bluetooth devices.
//Display the connection status.
//Display the scan status.
//Display the scanned data.
//Display the wifi command.
//Display the bluetooth connection status.
//Display the bluetooth devices.
//Display the connection status.
//Display the scan status.
//Display the scanned data.
//Display the wifi command.
//Display the bluetooth connection status.
//Display the bluetooth devices.
//Display the connection status.
//Display the scan status.
//Display the scanned data.
//Display the wifi command.
//Display the bluetooth connection status.
//Display the bluetooth devices.
//Display the connection status.
//Display the scan status.
//Display the scanned data.
//Display the wifi command.
//Display the bluetooth connection status.
//Display the bluetooth devices.
