package com.personal.gpssecurity.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gpssecurity.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    deviceId: String,
    onToggleTracking: (Boolean) -> Unit,
    isTrackingActive: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val prefs = remember { context.getSharedPreferences("gps_security_prefs", Context.MODE_PRIVATE) }

    // Periodic UI polling state for real-time telemetry updates when active
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(isTrackingActive) {
        if (isTrackingActive) {
            while (true) {
                kotlinx.coroutines.delay(2000)
                tick++
            }
        }
    }

    val lat = remember(tick, isTrackingActive) { prefs.getFloat("last_lat", 0f) }
    val lng = remember(tick, isTrackingActive) { prefs.getFloat("last_lng", 0f) }
    val accuracy = remember(tick, isTrackingActive) { prefs.getFloat("last_accuracy", 0f) }
    val speed = remember(tick, isTrackingActive) { prefs.getFloat("last_speed", 0f) }
    val altitude = remember(tick, isTrackingActive) { prefs.getFloat("last_altitude", 0f) }
    val address = remember(tick, isTrackingActive) { prefs.getString("last_address", "Waiting for GPS lock...") ?: "Waiting for GPS lock..." }

    var intervalSeconds by remember { mutableStateOf(prefs.getLong("tracking_interval_seconds", 30)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SENTINEL GPS GUARD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        containerColor = DarkBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECURE TRACKER CODE CARD (CRITICAL!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "YOUR SECURE TRACKER CODE",
                        color = Color(0xFF60A5FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(deviceId))
                                android.widget.Toast.makeText(context, "Code copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = deviceId,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Tap the code above to copy. Enter it in the 'Cloud Satellite Receiver' in your Web Dashboard to monitor this phone live on the map.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // MASTER ACTIVATION BUTTON
            Button(
                onClick = { onToggleTracking(!isTrackingActive) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTrackingActive) Color(0xFFDC2626) else Color(0xFF2563EB)
                )
            ) {
                Text(
                    text = if (isTrackingActive) "STOP GPS GUARD" else "START GPS GUARD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            // DEVICE MONITORING STATUS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (isTrackingActive) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Text(
                            text = "SECURITY ENGINE STATUS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = if (isTrackingActive) "SECURED & TRANSMITTING" else "SHIELD INACTIVE",
                        color = if (isTrackingActive) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (isTrackingActive) "Background high-precision GPS security is active and broadcasting live coordinate streams." else "Secure broadcasting is currently idle. Tap START GPS GUARD to protect your device.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            // REAL-TIME GPS TELEMETRY METRICS
            if (isTrackingActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "LIVE SATELLITE TELEMETRY",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Divider(color = Color.White.copy(alpha = 0.05f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("LATITUDE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(String.format("%.6f", lat), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("LONGITUDE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(String.format("%.6f", lng), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ACCURACY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", accuracy)} meters", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ALTITUDE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", altitude)} m", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Column {
                            Text("LAST ENCODED ADDRESS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(address, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)
                        }
                    }
                }
            }

            // TRANSMISSION PING INTERVAL CONFIGURATION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TRANSMISSION INTERVAL",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Configure how frequently your device sends GPS coordinates to the security database. Higher rates require more battery.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(10L to "Fast (10s)", 30L to "Normal (30s)", 120L to "Saver (2m)").forEach { (seconds, label) ->
                            val isSelected = intervalSeconds == seconds
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Color(0xFF3B82F6) else Color(0xFF0F172A),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        intervalSeconds = seconds
                                        prefs.edit().putLong("tracking_interval_seconds", seconds).apply()
                                        android.widget.Toast.makeText(context, "Interval set! Restart GPS Guard to apply.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // FAIL-SAFE RECOVERY INFORMATION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Fail-Safe Recovery Enabled",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "This application operates as a high-priority system service with boot-start permissions. If your phone restarts or loses power, tracking will automatically resume.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
