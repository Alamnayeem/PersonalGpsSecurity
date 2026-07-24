package com.charging.animation.pro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import com.charging.animation.pro.model.BatteryHealth
import com.charging.animation.pro.model.BatteryInfo
import com.charging.animation.pro.model.ChargingType
import com.charging.animation.pro.service.ChargingOverlayService

class BatteryReceiver(
    private val onBatteryChanged: ((BatteryInfo) -> Unit)? = null
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                val batteryInfo = extractBatteryInfo(intent, context)
                ChargingOverlayService.startOverlay(context, batteryInfo)
                onBatteryChanged?.invoke(batteryInfo)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                ChargingOverlayService.stopOverlay(context)
                val batteryInfo = extractBatteryInfo(intent, context)
                onBatteryChanged?.invoke(batteryInfo)
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val batteryInfo = extractBatteryInfo(intent, context)
                onBatteryChanged?.invoke(batteryInfo)
                if (batteryInfo.isCharging) {
                    ChargingOverlayService.updateBatteryInfo(context, batteryInfo)
                }
            }
        }
    }

    companion object {
        fun extractBatteryInfo(intent: Intent, context: Context): BatteryInfo {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 0

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargingType = when {
                !isCharging -> ChargingType.DISCONNECTED
                chargePlug == BatteryManager.BATTERY_PLUGGED_AC -> ChargingType.AC_FAST
                chargePlug == BatteryManager.BATTERY_PLUGGED_USB -> ChargingType.USB
                chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingType.WIRELESS
                else -> ChargingType.AC_STANDARD
            }

            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

            val healthRaw = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val health = when (healthRaw) {
                BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
                BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
                else -> BatteryHealth.UNSPECIFIED
            }

            // Estimate charging remaining minutes intelligently
            val remainingCapacity = 100 - percentage
            val estimatedMinutes = if (isCharging && remainingCapacity > 0) {
                when (chargingType) {
                    ChargingType.AC_FAST -> (remainingCapacity * 0.6f).toInt().coerceAtLeast(5)
                    ChargingType.AC_STANDARD -> (remainingCapacity * 1.0f).toInt().coerceAtLeast(10)
                    ChargingType.WIRELESS -> (remainingCapacity * 0.9f).toInt().coerceAtLeast(8)
                    ChargingType.USB -> (remainingCapacity * 1.8f).toInt().coerceAtLeast(15)
                    else -> 0
                }
            } else 0

            return BatteryInfo(
                percentage = percentage,
                isCharging = isCharging,
                chargingType = chargingType,
                voltageMillivolts = voltage,
                temperatureCelsius = temp,
                currentMilliampere = if (isCharging) 3500 else 0,
                health = health,
                estimatedMinutesRemaining = estimatedMinutes
            )
        }
    }
}
