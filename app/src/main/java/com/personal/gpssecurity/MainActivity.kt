package com.personal.gpssecurity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.personal.gpssecurity.ui.theme.PersonalGpsSecurityTheme
import com.personal.gpssecurity.ui.screens.DashboardScreen

class MainActivity : ComponentActivity() {

    private val prefs by lazy { getSharedPreferences("gps_security_prefs", Context.MODE_PRIVATE) }
    private var isTrackingActiveState by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineGranted || coarseGranted) {
            Toast.makeText(this, "Location permission granted. Ready to track.", Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocationPermission()
            }
        } else {
            Toast.makeText(this, "Warning: Location permissions required for safety mapping.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        isTrackingActiveState = isServiceRunning()

        var deviceId = prefs.getString("device_id", "") ?: ""
        if (deviceId.isEmpty()) {
            val randNum = (1000..9999).random()
            deviceId = "SENTINEL-$randNum"
            prefs.edit().putString("device_id", deviceId).apply()
        }

        setContent {
            PersonalGpsSecurityTheme {
                DashboardScreen(
                    deviceId = deviceId,
                    onToggleTracking = { active ->
                        toggleTrackingService(active)
                    },
                    isTrackingActive = isTrackingActiveState
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val missing = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (!granted) {
                        Toast.makeText(this, "Note: Background permission is required to track when the screen is OFF.", Toast.LENGTH_LONG).show()
                    }
                }.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun toggleTrackingService(enable: Boolean) {
        prefs.edit().putBoolean("is_tracking_active", enable).apply()
        isTrackingActiveState = enable
        val intent = Intent(this, TrackingService::class.java)
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else {
            stopService(intent)
        }
    }

    private fun isServiceRunning(): Boolean {
        return prefs.getBoolean("is_tracking_active", false)
    }
}
