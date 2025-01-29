package com.vc.myfirstwearapp.presentation

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class WifiServer (private val listener: WifiListener) {

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var serverJob: Job? = null
    private var writer: PrintWriter? = null
    private val port = 8080
    private val TAG = "WifiServer"

    interface WifiListener {
        fun onCommandReceived(command: String)
        fun onError(message: String)
    }

    fun start() {
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(port)
                Log.i(TAG, "Server started on port $port")
                while (serverJob?.isActive == true) {
                    clientSocket = serverSocket?.accept()
                    Log.i(TAG, "Client connected: ${clientSocket?.inetAddress?.hostAddress}")
                    handleClient(clientSocket)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error starting server: ${e.message}")
                withContext(Dispatchers.Main) {
                    listener.onError("Error starting server: ${e.message}")
                }
            }
        }
    }

    private fun handleClient(clientSocket: Socket?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
                writer = PrintWriter(clientSocket?.getOutputStream(), true)
                var inputLine: String?
                while (clientSocket?.isConnected == true) {
                    inputLine = reader.readLine()
                    if (inputLine == null) break
                    val command = inputLine ?: ""
                    Log.d(TAG, "Received command: $command")
                    withContext(Dispatchers.Main) {
                        listener.onCommandReceived(command)
                    }
                    // Example: Send a response back to the client
                    writer?.println("Command received: $command")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error handling client: ${e.message}")
                withContext(Dispatchers.Main) {
                    listener.onError("Error handling client: ${e.message}")
                }
            } finally {
                clientSocket?.close()
                Log.i(TAG, "Client disconnected")
            }
        }
    }

    fun sendDataToClient(data: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                writer?.println(data)
                Log.d(TAG, "Data sent to client: $data")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending data to client: ${e.message}")
                withContext(Dispatchers.Main) {
                    listener.onError("Error sending data to client: ${e.message}")
                }
            }
        }
    }

    fun close() {
        serverJob?.cancel()
        try {
            clientSocket?.close()
            serverSocket?.close()
            Log.i(TAG, "Server stopped")
        } catch (e: IOException) {
            Log.e(TAG, "Error closing server: ${e.message}")
        }
    }
}