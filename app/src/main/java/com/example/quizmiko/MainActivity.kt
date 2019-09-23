package com.example.quizmiko

import android.Manifest
import android.Manifest.permission.*

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.quizmiko.databinding.ActivityMainBinding
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    companion object {

        private val deviceId = "Player" + Random.nextInt(1, 50)
        private val REQUIRED_PERMISSIONS = arrayOf(
            BLUETOOTH,
            BLUETOOTH_ADMIN,
            ACCESS_WIFI_STATE,
            CHANGE_WIFI_STATE,
            ACCESS_COARSE_LOCATION
        )
        private const val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
        private val STRATEGY = Strategy.P2P_STAR
        private val TAG = "MikoQuiz"

        /** Returns true if the app was granted all the permissions. Otherwise, returns false.  */
        private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }
    }

    private lateinit var connectionsClient: ConnectionsClient

    // Callbacks for receiving payloads
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }
    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: endpoint '$endpointId' found, connecting")
            connectionsClient.requestConnection(deviceId, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {}
    }
    // Callbacks for connections to other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: accepting connection")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(TAG, "onConnectionResult: connection successful")
                connectionsClient.stopDiscovery()
                connectionsClient.stopAdvertising()
            } else {
                Log.i(TAG, "onConnectionResult: connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: disconnected from the opponent")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.currentStateTextView.text = getString(R.string.state_idle)
        binding.deviceName.text = deviceId

        connectionsClient = Nearby.getConnectionsClient(this)

        advertButton.setOnClickListener {
            startServer()
        }

        discButton.setOnClickListener {
            startClient()
        }


    }

    override fun onStart() {
        super.onStart()

        if (!hasPermissions(this, REQUIRED_PERMISSIONS.toString())) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
        }
    }

    override fun onStop() {
        connectionsClient.stopAllEndpoints()
        super.onStop()
    }

    fun startServer() {

        connectionsClient.startAdvertising(
            deviceId,
            packageName,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(
                STRATEGY
            ).build()
        ).addOnSuccessListener { Log.i(TAG, "startAdvertising()") }

    }

    fun startClient() {

        connectionsClient.startDiscovery(
            packageName, endpointDiscoveryCallback, DiscoveryOptions.Builder().setStrategy(
                STRATEGY
            ).build()
        ).addOnSuccessListener { Log.i(TAG, "startDiscovery()") }

    }


}