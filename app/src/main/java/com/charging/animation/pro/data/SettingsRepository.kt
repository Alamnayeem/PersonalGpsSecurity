package com.charging.animation.pro.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.charging.animation.pro.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "charging_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val ENABLE_OVERLAY = booleanPreferencesKey("enable_overlay")
        val ENABLE_ANIMATION = booleanPreferencesKey("enable_animation")
        val ENABLE_SOUND = booleanPreferencesKey("enable_sound")
        val ENABLE_VIBRATION = booleanPreferencesKey("enable_vibration")
        val AUTO_HIDE_SECONDS = intPreferencesKey("auto_hide_seconds")
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val ANIMATION_STYLE = stringPreferencesKey("animation_style")
        val OVERLAY_POSITION = stringPreferencesKey("overlay_position")
        val OVERLAY_SIZE = stringPreferencesKey("overlay_size")
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    val enableOverlay: Flow<Boolean> = context.dataStore.data.map { it[Keys.ENABLE_OVERLAY] ?: true }
    val enableAnimation: Flow<Boolean> = context.dataStore.data.map { it[Keys.ENABLE_ANIMATION] ?: true }
    val enableSound: Flow<Boolean> = context.dataStore.data.map { it[Keys.ENABLE_SOUND] ?: true }
    val enableVibration: Flow<Boolean> = context.dataStore.data.map { it[Keys.ENABLE_VIBRATION] ?: true }
    val autoHideSeconds: Flow<Int> = context.dataStore.data.map { it[Keys.AUTO_HIDE_SECONDS] ?: 8 }
    val animationSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.ANIMATION_SPEED] ?: 1.0f }

    val animationStyle: Flow<AnimationStyle> = context.dataStore.data.map {
        val name = it[Keys.ANIMATION_STYLE] ?: AnimationStyle.NEON_BLUE.name
        try { AnimationStyle.valueOf(name) } catch (e: Exception) { AnimationStyle.NEON_BLUE }
    }

    val overlayPosition: Flow<OverlayPosition> = context.dataStore.data.map {
        val name = it[Keys.OVERLAY_POSITION] ?: OverlayPosition.TOP_ISLAND.name
        try { OverlayPosition.valueOf(name) } catch (e: Exception) { OverlayPosition.TOP_ISLAND }
    }

    val overlaySize: Flow<OverlaySize> = context.dataStore.data.map {
        val name = it[Keys.OVERLAY_SIZE] ?: OverlaySize.STANDARD.name
        try { OverlaySize.valueOf(name) } catch (e: Exception) { OverlaySize.STANDARD }
    }

    val appTheme: Flow<AppTheme> = context.dataStore.data.map {
        val name = it[Keys.APP_THEME] ?: AppTheme.DARK.name
        try { AppTheme.valueOf(name) } catch (e: Exception) { AppTheme.DARK }
    }

    suspend fun setEnableOverlay(enabled: Boolean) { context.dataStore.edit { it[Keys.ENABLE_OVERLAY] = enabled } }
    suspend fun setEnableAnimation(enabled: Boolean) { context.dataStore.edit { it[Keys.ENABLE_ANIMATION] = enabled } }
    suspend fun setEnableSound(enabled: Boolean) { context.dataStore.edit { it[Keys.ENABLE_SOUND] = enabled } }
    suspend fun setEnableVibration(enabled: Boolean) { context.dataStore.edit { it[Keys.ENABLE_VIBRATION] = enabled } }
    suspend fun setAutoHideSeconds(seconds: Int) { context.dataStore.edit { it[Keys.AUTO_HIDE_SECONDS] = seconds } }
    suspend fun setAnimationSpeed(speed: Float) { context.dataStore.edit { it[Keys.ANIMATION_SPEED] = speed } }
    suspend fun setAnimationStyle(style: AnimationStyle) { context.dataStore.edit { it[Keys.ANIMATION_STYLE] = style.name } }
    suspend fun setOverlayPosition(pos: OverlayPosition) { context.dataStore.edit { it[Keys.OVERLAY_POSITION] = pos.name } }
    suspend fun setOverlaySize(size: OverlaySize) { context.dataStore.edit { it[Keys.OVERLAY_SIZE] = size.name } }
    suspend fun setAppTheme(theme: AppTheme) { context.dataStore.edit { it[Keys.APP_THEME] = theme.name } }
}
