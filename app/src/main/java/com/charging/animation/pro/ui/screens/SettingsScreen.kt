package com.charging.animation.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charging.animation.pro.model.AnimationStyle
import com.charging.animation.pro.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val enableOverlay by settingsViewModel.enableOverlay.collectAsState()
    val enableSound by settingsViewModel.enableSound.collectAsState()
    val enableVibration by settingsViewModel.enableVibration.collectAsState()
    val autoHide by settingsViewModel.autoHideSeconds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Preferences", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)

        SettingToggle("Enable Floating Overlay", enableOverlay) { settingsViewModel.toggleOverlay(it) }
        SettingToggle("Connection Sound Chime", enableSound) { settingsViewModel.toggleSound(it) }
        SettingToggle("Connection Haptic Vibration", enableVibration) { settingsViewModel.toggleVibration(it) }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Auto Hide Duration", fontSize = 14.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 8, 15, 0).forEach { sec ->
                FilterChip(
                    selected = autoHide == sec,
                    onClick = { settingsViewModel.setAutoHide(sec) },
                    label = { Text(if (sec == 0) "Never" else "${sec}s") }
                )
            }
        }
    }
}

@Composable
fun SettingToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131B2E), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
