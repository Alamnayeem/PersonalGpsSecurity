package com.charging.animation.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charging.animation.pro.model.AnimationStyle
import com.charging.animation.pro.service.ChargingOverlayService
import com.charging.animation.pro.viewmodel.BatteryViewModel
import com.charging.animation.pro.viewmodel.SettingsViewModel

@Composable
fun PreviewScreen(
    batteryViewModel: BatteryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    val currentStyle by settingsViewModel.animationStyle.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Live Overlay Simulator",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Style Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimationStyle.values().take(3).forEach { style ->
                FilterChip(
                    selected = currentStyle == style,
                    onClick = { settingsViewModel.setStyle(style) },
                    label = { Text(style.displayName) }
                )
            }
        }

        // Big Simulation Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF131B2E), shape = RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⚡ OVERLAY ACTIVE",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Style: ${currentStyle.displayName}",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
