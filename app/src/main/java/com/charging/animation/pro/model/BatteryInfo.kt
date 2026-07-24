package com.charging.animation.pro.model

enum class ChargingType {
    AC_FAST,
    AC_STANDARD,
    USB,
    WIRELESS,
    DISCONNECTED
}

enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    WARM,
    COLD,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED
}

data class BatteryInfo(
    val percentage: Int = 0,
    val isCharging: Boolean = false,
    val chargingType: ChargingType = ChargingType.DISCONNECTED,
    val voltageMillivolts: Int = 0,
    val temperatureCelsius: Float = 0f,
    val currentMilliampere: Int = 0,
    val health: BatteryHealth = BatteryHealth.GOOD,
    val estimatedMinutesRemaining: Int = 0
) {
    val formattedTemperature: String
        get() = String.format("%.1f°C", temperatureCelsius)

    val formattedVoltage: String
        get() = String.format("%.2f V", voltageMillivolts / 1000f)

    val chargingSpeedLabel: String
        get() = when (chargingType) {
            ChargingType.AC_FAST -> "Fast Charging 67W+"
            ChargingType.AC_STANDARD -> "AC Charging"
            ChargingType.USB -> "USB Cable Charging"
            ChargingType.WIRELESS -> "Wireless Super Fast"
            ChargingType.DISCONNECTED -> "Not Charging"
        }
}
