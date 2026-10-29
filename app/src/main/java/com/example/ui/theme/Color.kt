package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Palette — Warm Alabaster & Editorial Slate
val WarmWhiteBackground = Color(0xFFFAF9F5)
val WarmSurfaceElevated = Color(0xFFFFFFFF)
val WarmSurfaceSubtle = Color(0xFFF3F1EC)
val WarmBorderHairline = Color(0xFFE6E2D8)
val PrimaryInkLight = Color(0xFF14151A)
val SecondaryInkLight = Color(0xFF5E626E)
val TertiaryInkLight = Color(0xFF8C909E)

// Dark Mode Palette — Near-Black Obsidian & Graphite
val NearBlackBackground = Color(0xFF0B0C0E)
val DarkSurfaceElevated = Color(0xFF14161B)
val DarkSurfaceSubtle = Color(0xFF1C1F26)
val DarkBorderHairline = Color(0xFF262A34)
val PrimaryInkDark = Color(0xFFF4F4F6)
val SecondaryInkDark = Color(0xFF9498A6)
val TertiaryInkDark = Color(0xFF636775)

// Core Semantic Accents (Muted Luxury Tone)
val DeepIndigoLight = Color(0xFF3F4E8C)
val DeepIndigoDark = Color(0xFF7384D4)
val DeepIndigoContainerLight = Color(0xFFEAEFF7)
val DeepIndigoContainerDark = Color(0xFF21294A)

val EmeraldAccentLight = Color(0xFF1E7A5F)
val EmeraldAccentDark = Color(0xFF38B28E)
val EmeraldContainerLight = Color(0xFFE3F3EE)
val EmeraldContainerDark = Color(0xFF13382D)

val SoftGreenSuccess = Color(0xFF2E8B57)
val SoftGreenDark = Color(0xFF48B87A)

val AmberWarning = Color(0xFFC88A2B)
val AmberWarningDark = Color(0xFFE0A445)

val MutedRedDanger = Color(0xFFB84A4A)
val MutedRedDangerDark = Color(0xFFD96B6B)

// Curated Habit & Category Luxury Swatches (Avoids childish neon/saturated colors)
object AuraPalette {
    val Swatches: List<Pair<String, Long>> = listOf(
        "Deep Indigo" to 0xFF3F4E8C,
        "Emerald Sage" to 0xFF1E7A5F,
        "Warm Terracotta" to 0xFFB55D4C,
        "Gilded Amber" to 0xFFC28429,
        "Slate Violet" to 0xFF665687,
        "Nordic Teal" to 0xFF2C6E7A,
        "Rosewood" to 0xFF944D63,
        "Espresso Bronze" to 0xFF755C48,
        "Midnight Cobalt" to 0xFF2D4A73,
        "Olive Moss" to 0xFF546E47
    )

    fun parseHex(hex: String, fallback: Color = DeepIndigoLight): Color {
        return try {
            val cleaned = hex.trim().removePrefix("#")
            val longVal = cleaned.toLong(16)
            if (cleaned.length == 6) {
                Color(longVal or 0xFF000000)
            } else {
                Color(longVal)
            }
        } catch (_: Exception) {
            fallback
        }
    }
}
