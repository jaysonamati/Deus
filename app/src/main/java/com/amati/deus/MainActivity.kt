package com.amati.deus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.amati.deus.databinding.ActivityMainBinding
import com.amati.deus.services.ServiceActions
import com.amati.deus.services.ServiceState
import com.amati.deus.services.WatchingService
import com.amati.deus.services.getServiceState
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        actionOnService(ServiceActions.START)

//        if (locationPermissionApproved()){
//            WatchingService().subscribeToLocationUpdates()
//        }else{
//            requestLocationPermission()
//        }
    }

    private fun actionOnService(action: ServiceActions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == ServiceActions.STOP) return
        Intent(this, WatchingService::class.java).also {
            it.action = action.name
            startForegroundService(it)
            return
        }
    }

    fun startLocationUpdates() {

    }

    private fun locationPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestLocationPermission() {
        val provideRationale = locationPermissionApproved()

        if (provideRationale) {
            Snackbar.make(
                binding.root,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction("OK") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        Companion.REQUEST_LOCATION_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Timber.d("Request location only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Companion.REQUEST_LOCATION_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.d("onRequestPermissionResult")
        when (requestCode) {
            REQUEST_LOCATION_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Timber.d("User interaction was cancelled")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    WatchingService().subscribeToLocationUpdates()
                else -> {
                    // Permission denied
                    Snackbar.make(
                        binding.root,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Settings") {
                            // Build intent that displays the App settings screen
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }

            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }
}