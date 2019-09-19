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
import android.util.Log
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

        private const val LOG_TAG = "MikoQuiz"

        private const val packgName = "com.example.quizmiko"

        private val deviceId = "Player " + Random.nextInt(1, 10)

        private lateinit var advOptions : AdvertisingOptions

        private lateinit var discOptions : DiscoveryOptions


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
        advOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        discOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()


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
            packgName,
            object : ConnectionLifecycleCallback() {
                override fun onConnectionResult(
                    endpointId: String,
                    result: ConnectionResolution
                ) {
                    when (result.status.statusCode) {
                        ConnectionsStatusCodes.STATUS_OK -> Log.i(
                            LOG_TAG,
                            "Connection to $endpointId was successful"
                        )

                        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Log.i(
                            LOG_TAG,
                            "Connection attempt to $endpointId was rejected"
                        )
                        ConnectionsStatusCodes.STATUS_ERROR -> Log.i(
                            LOG_TAG,
                            "Connection to $endpointId failed"
                        )
                        else -> Log.i(
                            LOG_TAG,
                            "Unknown status code"
                        )
                    }
                }

                override fun onDisconnected(endpointId: String) {
                    Log.d(
                        LOG_TAG,
                        "Disconnected from: $endpointId."
                    )
                }

                override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
                    connClient.acceptConnection(p0, object : PayloadCallback() {
                        override fun onPayloadReceived(p0: String, p1: Payload) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    })
                }
            },
            advOptions
        ).addOnSuccessListener {
            Log.i(LOG_TAG, "$deviceId started advertising.")
            currentStateTextView.text = "advertising"
        }.addOnFailureListener { exception ->
            Log.i(LOG_TAG, "$deviceId failed to advertise.")
        }

    }

    private fun startClient() {
        connClient.startDiscovery(deviceId, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, p1: DiscoveredEndpointInfo) {
                Log.d(
                    LOG_TAG,
                    "Endpoint found: $endpointId"
                )
                connClient.requestConnection(
                    deviceId,
                    endpointId,
                    object : ConnectionLifecycleCallback() {
                        override fun onConnectionResult(
                            endpointId: String,
                            result: ConnectionResolution
                        ) {
                            when (result.status.statusCode) {
                                ConnectionsStatusCodes.STATUS_OK -> Log.i(
                                    LOG_TAG,
                                    "Connection to $endpointId was successful"
                                )

                                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Log.i(
                                    LOG_TAG,
                                    "Connection attempt to $endpointId was rejected"
                                )
                                ConnectionsStatusCodes.STATUS_ERROR -> Log.i(
                                    LOG_TAG,
                                    "Connection to $endpointId failed"
                                )
                                else -> Log.i(
                                    LOG_TAG,
                                    "Unknown status code"
                                )
                            }
                        }

                        override fun onDisconnected(endpointId: String) {
                            Log.d(
                                LOG_TAG,
                                "Disconnected from: $endpointId."
                            )
                        }

                        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
                            connClient.acceptConnection(p0, object : PayloadCallback() {
                                override fun onPayloadReceived(p0: String, p1: Payload) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }
                            })
                        }
                    })
                    .addOnSuccessListener {
                        Log.d(
                            LOG_TAG,
                            "Connection to $deviceId accepted."
                        )
                    }.addOnFailureListener { e ->
                        Log.d(
                            LOG_TAG,
                            "Connection to $deviceId failed. $e "
                        )
                    }
            }

            override fun onEndpointLost(p0: String) {
                Log.d(
                    LOG_TAG,
                    "Connection to $deviceId, was lost."
                )
            }
        }, discOptions)
            .addOnSuccessListener {
                currentStateTextView.text = "discovering"
                Log.i(LOG_TAG, "$deviceId started discovering.")
            }.addOnFailureListener { e ->
                Log.i(LOG_TAG, "$e")
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