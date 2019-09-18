package com.example.quizmiko

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.nearby.Nearby
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.content.Context
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.quizmiko.databinding.ActivityMainBinding
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.Strategy.P2P_STAR
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            permission.BLUETOOTH,
            BLUETOOTH_ADMIN,
            ACCESS_WIFI_STATE,
            CHANGE_WIFI_STATE,
            ACCESS_COARSE_LOCATION
        )
        private const val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

        private val STRATEGY = P2P_STAR


        private val deviceId = "Player " + Random.nextInt(1, 10)

        private val advOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()

        private val discOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()


    }

    lateinit var connClient: ConnectionsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connClient = Nearby.getConnectionsClient(this)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.currentStateTextView.text = getString(R.string.state_idle)
        binding.deviceName.text = deviceId


        advertButton.setOnClickListener {
            startServer()
            //joinButton.isEnabled = false
        }


        discButton.setOnClickListener {
            startClient()
            //startButton.isEnabled = false
        }

        disconnectButton.setOnClickListener {
            connClient.apply {
                stopAdvertising()
                stopDiscovery()
                stopAllEndpoints()
            }
            currentStateTextView.text = "Idle"
        }


    }

    override fun onStart() {
        super.onStart()

        if (!hasPermissions(this, REQUIRED_PERMISSIONS.toString())) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
        }
    }

    override fun onStop() {
        connClient.stopAllEndpoints()

        super.onStop()
    }

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


    private fun startServer() {

        connClient.startAdvertising(
            deviceId,
            packageName,
            connectionLifecycleCallback,
            advOptions
        ).addOnSuccessListener {
            Toast.makeText(
                applicationContext,
                "Advertising started",
                Toast.LENGTH_SHORT
            ).show()
            currentStateTextView.text = "advertising"
        }.addOnFailureListener { exception ->
            Toast.makeText(
                applicationContext,
                "Advertising failed, exception thrown: ${exception.message}.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun startClient() {
        connClient.startDiscovery(deviceId, endpointDiscoveryCallback, discOptions)
            .addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "Discovering started",
                    Toast.LENGTH_SHORT
                ).show()
                currentStateTextView.text = "discovering"

            }.addOnFailureListener { exception ->
                Toast.makeText(
                    applicationContext,
                    "Discovery failed, exception thrown: ${exception.message}.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            currentStateTextView.text = "Endpoint found - $endpointId"
            connClient.requestConnection(deviceId, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "Requested connection to $endpointId !",
                        Toast.LENGTH_SHORT
                    ).show()
                    currentStateTextView.text = "Found : $endpointId"
                }
                .addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        "Connection request to $endpointId failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        override fun onEndpointLost(endpointId: String) {
            Toast.makeText(applicationContext, "Endpoint: $endpointId lost!", Toast.LENGTH_SHORT)
                .show()
        }

    }


    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> Toast.makeText(
                    applicationContext,
                    "Connected to $endpointId successfully",
                    Toast.LENGTH_SHORT
                ).show()

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Toast.makeText(
                    applicationContext,
                    "Connection attempt to $endpointId was rejected",
                    Toast.LENGTH_SHORT
                ).show()
                ConnectionsStatusCodes.STATUS_ERROR -> Toast.makeText(
                    applicationContext,
                    "Connected attempt to $endpointId failed",
                    Toast.LENGTH_SHORT
                ).show()
                else -> Toast.makeText(
                    applicationContext,
                    "Unknown status code",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onDisconnected(p0: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {

            Toast.makeText(applicationContext, "Connection with $p0 initiated.", Toast.LENGTH_SHORT)
                .show()
            connClient.acceptConnection(p0, payloadCallback)
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }


}