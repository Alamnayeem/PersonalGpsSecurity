package com.personal.gpssecurity

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.personal.gpssecurity.data.LocationDatabase
import com.personal.gpssecurity.data.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val NOTIFICATION_CHANNEL_ID = "gps_security_tracking_channel"
    private val NOTIFICATION_ID = 4412

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GPS_Security::TrackingWakelock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes fallback*/)

        createNotificationChannel()
        setupLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Secured Mode Active", "Monitoring device GPS coordinates...")
        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()
        return START_STICKY
    }

    private fun setupLocationUpdates() {
        val intervalMs = getSharedPreferences("gps_security_prefs", Context.MODE_PRIVATE)
            .getFloat("tracking_interval_seconds", 30f).toLong() * 1000

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    serviceScope.launch {
                        val geocoder = Geocoder(applicationContext, Locale.getDefault())
                        var addressStr = "GPS Signal Acquired"
                        try {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                addressStr = addresses[0].getAddressLine(0) ?: "Unknown Location Address"
                            }
                        } catch (e: Exception) {
                            addressStr = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                        }

                        val db = LocationDatabase.getInstance(applicationContext)
                        db.locationDao().insertLocation(
                            LocationEntity(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                timestamp = System.currentTimeMillis(),
                                address = addressStr,
                                speed = location.speed,
                                altitude = location.altitude
                            )
                        )

                        // Update shared preferences with latest coordinates for the UI to display
                        val securityPrefs = getSharedPreferences("gps_security_prefs", Context.MODE_PRIVATE)
                        securityPrefs.edit().apply {
                            putFloat("last_lat", location.latitude.toFloat())
                            putFloat("last_lng", location.longitude.toFloat())
                            putFloat("last_accuracy", location.accuracy)
                            putFloat("last_speed", location.speed)
                            putFloat("last_altitude", location.altitude.toFloat())
                            putString("last_address", addressStr)
                            putLong("last_update_time", System.currentTimeMillis())
                            apply()
                        }

                        // Sync to Firestore Cloud REST API
                        syncLocationToCloud(
                            lat = location.latitude,
                            lng = location.longitude,
                            accuracy = location.accuracy,
                            speed = location.speed,
                            altitude = location.altitude,
                            address = addressStr
                        )
                    }
                }
            }
        }
    }

    private fun syncLocationToCloud(
        lat: Double,
        lng: Double,
        accuracy: Float,
        speed: Float,
        altitude: Double,
        address: String
    ) {
        val prefs = getSharedPreferences("gps_security_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", "") ?: ""
        if (deviceId.isEmpty()) return

        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        val batteryLevel = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = false // simple fallback

        val urlStr = "https://firestore.googleapis.com/v1/projects/mercurial-adviser-8fs6l/databases/ai-studio-personalgpssecur-673a77a4-dd1f-4e38-b2df-90b0c0b17acb/documents/devices/$deviceId"

        val jsonPayload = """
        {
          "fields": {
            "deviceId": { "stringValue": "$deviceId" },
            "ownerName": { "stringValue": "Android Phone" },
            "medicalInfo": { "stringValue": "GPS tracker active on device." },
            "latitude": { "doubleValue": $lat },
            "longitude": { "doubleValue": $lng },
            "accuracy": { "doubleValue": $accuracy },
            "speed": { "doubleValue": $speed },
            "altitude": { "doubleValue": $altitude },
            "address": { "stringValue": "$address" },
            "batteryLevel": { "integerValue": "$batteryLevel" },
            "isCharging": { "booleanValue": $isCharging },
            "isGpsEnabled": { "booleanValue": true },
            "isInternetConnected": { "booleanValue": true },
            "isTrackingActive": { "booleanValue": true },
            "lastUpdate": { "stringValue": "${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date())}" }
          }
        }
        """.trimIndent()

        try {
            val url = java.net.URL(urlStr)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            conn.outputStream.use { os ->
                val input = jsonPayload.toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            if (responseCode == 200 || responseCode == 201) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val alarmPlaying = responseText.contains("\"alarmPlaying\": {\n            \"booleanValue\": true") || responseText.contains("\"alarmPlaying\": {\"booleanValue\": true}")
                if (alarmPlaying) {
                    triggerSirenVibration()
                }
            } else {
                val errText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                android.util.Log.e("TrackingService", "Cloud sync failed (code $responseCode): $errText")
            }
            conn.disconnect()
        } catch (e: Exception) {
            android.util.Log.e("TrackingService", "Cloud sync network error", e)
        }
    }

    private fun triggerSirenVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000).build(),
                locationCallback,
                mainLooper
            )
        } catch (unlikely: SecurityException) {
            // No permission granted
        }
    }

    private fun createNotification(title: String, text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Device GPS Guard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
