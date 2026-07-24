package com.charging.animation.pro.model

enum class AnimationStyle(
    val displayName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val glowColorHex: Long
) {
    NEON_BLUE("Neon Blue", 0xFF00E5FF, 0xFF2979FF, 0x9900E5FF),
    GREEN_ENERGY("Green Energy", 0xFF00E676, 0xFF00B0FF, 0x9900E676),
    PURPLE_GLOW("Purple Glow", 0xFFD500F9, 0xFF651FFF, 0x99D500F9),
    ORANGE_FIRE("Orange Fire", 0xFFFF9100, 0xFFFF3D00, 0x99FF9100),
    CYBERPUNK("Cyberpunk HUD", 0xFFFF007F, 0xFF00F0FF, 0xB3FF007F),
    MINIMAL("Minimal Glass", 0xFFFFFFFF, 0xFFB0BEC5, 0x66FFFFFF),
    NOTHING_DOT("Nothing OS Dot", 0xFFFF2D55, 0xFFFFFFFF, 0x80FF2D55)
}

enum class OverlayPosition {
    TOP_ISLAND,
    CENTER_FLOAT,
    BOTTOM_DOCK
}

enum class OverlaySize {
    COMPACT,
    STANDARD,
    EXPANDED
}

enum class AppTheme {
    DARK,
    LIGHT,
    AMOLED
}
