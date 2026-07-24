package com.charging.animation.pro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charging.animation.pro.data.SettingsRepository
import com.charging.animation.pro.model.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val enableOverlay = repository.enableOverlay.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val enableAnimation = repository.enableAnimation.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val enableSound = repository.enableSound.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val enableVibration = repository.enableVibration.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val autoHideSeconds = repository.autoHideSeconds.stateIn(viewModelScope, SharingStarted.Lazily, 8)
    val animationSpeed = repository.animationSpeed.stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)
    val animationStyle = repository.animationStyle.stateIn(viewModelScope, SharingStarted.Lazily, AnimationStyle.NEON_BLUE)
    val overlayPosition = repository.overlayPosition.stateIn(viewModelScope, SharingStarted.Lazily, OverlayPosition.TOP_ISLAND)
    val overlaySize = repository.overlaySize.stateIn(viewModelScope, SharingStarted.Lazily, OverlaySize.STANDARD)
    val appTheme = repository.appTheme.stateIn(viewModelScope, SharingStarted.Lazily, AppTheme.DARK)

    fun toggleOverlay(enabled: Boolean) = viewModelScope.launch { repository.setEnableOverlay(enabled) }
    fun toggleAnimation(enabled: Boolean) = viewModelScope.launch { repository.setEnableAnimation(enabled) }
    fun toggleSound(enabled: Boolean) = viewModelScope.launch { repository.setEnableSound(enabled) }
    fun toggleVibration(enabled: Boolean) = viewModelScope.launch { repository.setEnableVibration(enabled) }
    fun setAutoHide(seconds: Int) = viewModelScope.launch { repository.setAutoHideSeconds(seconds) }
    fun setSpeed(speed: Float) = viewModelScope.launch { repository.setAnimationSpeed(speed) }
    fun setStyle(style: AnimationStyle) = viewModelScope.launch { repository.setAnimationStyle(style) }
    fun setPosition(pos: OverlayPosition) = viewModelScope.launch { repository.setOverlayPosition(pos) }
    fun setSize(size: OverlaySize) = viewModelScope.launch { repository.setOverlaySize(size) }
    fun setTheme(theme: AppTheme) = viewModelScope.launch { repository.setAppTheme(theme) }
}
