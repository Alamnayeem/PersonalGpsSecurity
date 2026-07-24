package com.charging.animation.pro.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.charging.animation.pro.ChargingApplication
import com.charging.animation.pro.model.BatteryInfo
import com.charging.animation.pro.model.ChargingType

class ChargingOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private val autoHideHandler = Handler(Looper.getMainLooper())
    private var autoHideRunnable: Runnable? = null

    private var batteryInfoState = mutableStateOf(BatteryInfo())

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SHOW -> {
                    val info = extractBatteryInfoFromIntent(it)
                    batteryInfoState.value = info
                    showOverlayWindow()
                    scheduleAutoHide(10000)
                }
                ACTION_UPDATE -> {
                    batteryInfoState.value = extractBatteryInfoFromIntent(it)
                }
                ACTION_HIDE -> {
                    removeOverlayWindow()
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlayWindow() {
        if (overlayView != null) return

        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "⚠️ Please allow 'Display over other apps' permission to show charging animation!", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 48
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ChargingOverlayService)
            setViewTreeViewModelStoreOwner(this@ChargingOverlayService)
            setViewTreeSavedStateRegistryOwner(this@ChargingOverlayService)
            setContent {
                FloatingChargingIsland(batteryInfo = batteryInfoState.value)
            }
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not draw overlay: " + e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleAutoHide(delayMs: Long) {
        autoHideRunnable?.let { autoHideHandler.removeCallbacks(it) }
        if (delayMs > 0) {
            autoHideRunnable = Runnable { removeOverlayWindow() }
            autoHideHandler.postDelayed(autoHideRunnable!!, delayMs)
        }
    }

    private fun removeOverlayWindow() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, ChargingApplication.CHANNEL_ID)
            .setContentTitle("Charging Animation Active ⚡")
            .setContentText("Listening for charger connection & showing floating island")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun extractBatteryInfoFromIntent(intent: Intent): BatteryInfo {
        val pct = intent.getIntExtra(EXTRA_PERCENTAGE, 50)
        val isCharging = intent.getBooleanExtra(EXTRA_IS_CHARGING, true)
        val temp = intent.getFloatExtra(EXTRA_TEMP, 33.5f)
        val volt = intent.getIntExtra(EXTRA_VOLT, 4150)
        return BatteryInfo(
            percentage = pct,
            isCharging = isCharging,
            chargingType = ChargingType.AC_FAST,
            voltageMillivolts = volt,
            temperatureCelsius = temp,
            estimatedMinutesRemaining = 25
        )
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
        removeOverlayWindow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_SHOW = "com.charging.animation.pro.SHOW"
        const val ACTION_HIDE = "com.charging.animation.pro.HIDE"
        const val ACTION_UPDATE = "com.charging.animation.pro.UPDATE"

        const val EXTRA_PERCENTAGE = "extra_percentage"
        const val EXTRA_IS_CHARGING = "extra_is_charging"
        const val EXTRA_TEMP = "extra_temp"
        const val EXTRA_VOLT = "extra_volt"

        fun startOverlay(context: Context, info: BatteryInfo) {
            val intent = Intent(context, ChargingOverlayService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_PERCENTAGE, info.percentage)
                putExtra(EXTRA_IS_CHARGING, info.isCharging)
                putExtra(EXTRA_TEMP, info.temperatureCelsius)
                putExtra(EXTRA_VOLT, info.voltageMillivolts)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateBatteryInfo(context: Context, info: BatteryInfo) {
            val intent = Intent(context, ChargingOverlayService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_PERCENTAGE, info.percentage)
                putExtra(EXTRA_IS_CHARGING, info.isCharging)
            }
            context.startService(intent)
        }

        fun stopOverlay(context: Context) {
            val intent = Intent(context, ChargingOverlayService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }
    }
}

@Composable
fun FloatingChargingIsland(batteryInfo: BatteryInfo) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Dynamic Island Glass Card
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xE60A192F),
                            Color(0xCC030C1A)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = glowAlpha),
                            Color(0xFF2979FF).copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Battery Percentage Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFF00E5FF).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .border(
                            2.dp,
                            Color(0xFF00E5FF).copy(alpha = glowAlpha),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${batteryInfo.percentage}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Charging details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ " + batteryInfo.chargingSpeedLabel,
                            color = Color(0xFF00E5FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${batteryInfo.formattedVoltage} • ${batteryInfo.formattedTemperature} • ~${batteryInfo.estimatedMinutesRemaining}m left",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
