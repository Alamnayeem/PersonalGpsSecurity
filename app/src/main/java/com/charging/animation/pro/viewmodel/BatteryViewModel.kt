package com.charging.animation.pro.viewmodel

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charging.animation.pro.model.BatteryInfo
import com.charging.animation.pro.receiver.BatteryReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val batteryReceiver = BatteryReceiver { info ->
        _batteryInfo.value = info
    }

    init {
        refreshBatteryInfo()
        registerReceiver()
    }

    fun refreshBatteryInfo() {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = getApplication<Application>().registerReceiver(null, intentFilter)
        batteryStatus?.let {
            val info = BatteryReceiver.extractBatteryInfo(it, getApplication())
            _batteryInfo.value = info
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        try {
            getApplication<Application>().registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onCleared()
    }
}
