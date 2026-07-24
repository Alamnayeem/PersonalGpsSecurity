package com.charging.animation.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charging.animation.pro.viewmodel.BatteryViewModel
import com.charging.animation.pro.viewmodel.SettingsViewModel

@Composable
fun HomeScreen(
    batteryViewModel: BatteryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    val isOverlayEnabled by settingsViewModel.enableOverlay.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Charging Animation Pro",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Floating Glassmorphic Overlay",
                    fontSize = 13.sp,
                    color = Color(0xFF00E5FF)
                )
            }
            Switch(
                checked = isOverlayEnabled,
                onCheckedChange = { settingsViewModel.toggleOverlay(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00E5FF)
                )
            )
        }

        // Battery Main Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF131B2E), Color(0xFF1E293B))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${batteryInfo.percentage}%",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = if (batteryInfo.isCharging) "⚡ Charging Active" else "🔋 Disconnected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = batteryInfo.chargingSpeedLabel,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Diagnostics Grid
        Text(
            text = "BATTERY DIAGNOSTICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                modifier = Modifier.weight(1f),
                title = "Voltage",
                value = batteryInfo.formattedVoltage,
                icon = Icons.Default.ElectricBolt,
                color = Color(0xFFFF9100)
            )
            StatBox(
                modifier = Modifier.weight(1f),
                title = "Temperature",
                value = batteryInfo.formattedTemperature,
                icon = Icons.Default.Thermostat,
                color = Color(0xFFD500F9)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                modifier = Modifier.weight(1f),
                title = "Health",
                value = batteryInfo.health.name,
                icon = Icons.Default.Favorite,
                color = Color(0xFF00E676)
            )
            StatBox(
                modifier = Modifier.weight(1f),
                title = "Time Left",
                value = "~${batteryInfo.estimatedMinutesRemaining} min",
                icon = Icons.Default.Timer,
                color = Color(0xFF00B0FF)
            )
        }
    }
}

@Composable
fun StatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Box(
        modifier = modifier
            .background(Color(0xFF131B2E), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
