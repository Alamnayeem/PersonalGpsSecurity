package com.charging.animation.pro

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.charging.animation.pro.ui.screens.HomeScreen
import com.charging.animation.pro.ui.screens.PermissionsScreen
import com.charging.animation.pro.ui.screens.PreviewScreen
import com.charging.animation.pro.ui.screens.SettingsScreen
import com.charging.animation.pro.ui.theme.ChargingAnimationProTheme
import com.charging.animation.pro.viewmodel.BatteryViewModel
import com.charging.animation.pro.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val batteryViewModel: BatteryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChargingAnimationProTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                val hasOverlayPermission = remember { mutableStateOf(checkOverlayPermission()) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!hasOverlayPermission.value) {
                        PermissionsScreen(
                            onRequestPermission = {
                                requestOverlayPermission()
                            },
                            onContinueAnyway = {
                                hasOverlayPermission.value = true
                            }
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color(0xFF0F172A),
                                    contentColor = Color.White
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        icon = { Icon(Icons.Default.Bolt, contentDescription = "Home") },
                                        label = { Text("Dashboard") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Preview") },
                                        label = { Text("Preview") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text("Settings") }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (selectedTab) {
                                    0 -> HomeScreen(batteryViewModel, settingsViewModel)
                                    1 -> PreviewScreen(batteryViewModel, settingsViewModel)
                                    2 -> SettingsScreen(settingsViewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1234)
        }
    }

    override fun onResume() {
        super.onResume()
        batteryViewModel.refreshBatteryInfo()
    }
}
