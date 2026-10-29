package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Design System Scales: Spacing, Corner Radius, Shadow, Animation Timing, Opacity
object AuraTokens {
    object Spacing {
        val xxs: Dp = 4.dp
        val xs: Dp = 8.dp
        val sm: Dp = 12.dp
        val md: Dp = 16.dp
        val lg: Dp = 20.dp
        val xl: Dp = 24.dp
        val xxl: Dp = 32.dp
        val hero: Dp = 44.dp
    }

    object Radius {
        val xs: Dp = 8.dp
        val sm: Dp = 12.dp
        val md: Dp = 16.dp
        val lg: Dp = 22.dp
        val xl: Dp = 28.dp
        val pill: Dp = 999.dp
    }

    object Elevation {
        val flat: Dp = 0.dp
        val subtle: Dp = 1.dp
        val card: Dp = 2.dp
        val floating: Dp = 8.dp
        val modal: Dp = 16.dp
    }

    object Opacity {
        const val GlassSurface = 0.86f
        const val GlassBorder = 0.14f
        const val SubtleTint = 0.11f
        const val MutedElement = 0.62f
        const val Disabled = 0.38f
    }

    object Motion {
        const val FastMs = 180
        const val MediumMs = 280
        const val SlowMs = 420

        fun <T> springSnappy() = spring<T>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        fun <T> springBouncy() = spring<T>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    }
}

@Immutable
data class AuraExtendedColors(
    val isDark: Boolean,
    val backgroundPrimary: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val borderHairline: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val primaryIndigo: Color,
    val accentEmerald: Color,
    val successGreen: Color,
    val warningAmber: Color,
    val dangerRed: Color,
    val glassBackground: Color
)

val LocalAuraColors = staticCompositionLocalOf {
    AuraExtendedColors(
        isDark = false,
        backgroundPrimary = WarmWhiteBackground,
        surfaceElevated = WarmSurfaceElevated,
        surfaceSubtle = WarmSurfaceSubtle,
        borderHairline = WarmBorderHairline,
        inkPrimary = PrimaryInkLight,
        inkSecondary = SecondaryInkLight,
        inkTertiary = TertiaryInkLight,
        primaryIndigo = DeepIndigoLight,
        accentEmerald = EmeraldAccentLight,
        successGreen = SoftGreenSuccess,
        warningAmber = AmberWarning,
        dangerRed = MutedRedDanger,
        glassBackground = WarmSurfaceElevated.copy(alpha = 0.90f)
    )
}

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigoLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8ECF7),
    onPrimaryContainer = DeepIndigoLight,
    secondary = EmeraldAccentLight,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = EmeraldAccentLight,
    tertiary = AmberWarning,
    background = WarmWhiteBackground,
    onBackground = PrimaryInkLight,
    surface = WarmSurfaceElevated,
    onSurface = PrimaryInkLight,
    surfaceVariant = WarmSurfaceSubtle,
    onSurfaceVariant = SecondaryInkLight,
    outline = WarmBorderHairline,
    error = MutedRedDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DeepIndigoDark,
    onPrimary = Color(0xFF0B0C0E),
    primaryContainer = DeepIndigoContainerDark,
    onPrimaryContainer = Color(0xFFDCE2FF),
    secondary = EmeraldAccentDark,
    onSecondary = Color(0xFF0B0C0E),
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = Color(0xFFB8F2DF),
    tertiary = AmberWarningDark,
    background = NearBlackBackground,
    onBackground = PrimaryInkDark,
    surface = DarkSurfaceElevated,
    onSurface = PrimaryInkDark,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = SecondaryInkDark,
    outline = DarkBorderHairline,
    error = MutedRedDangerDark,
    onError = Color(0xFF0B0C0E)
)

val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(AuraTokens.Radius.xs),
    small = RoundedCornerShape(AuraTokens.Radius.sm),
    medium = RoundedCornerShape(AuraTokens.Radius.md),
    large = RoundedCornerShape(AuraTokens.Radius.lg),
    extraLarge = RoundedCornerShape(AuraTokens.Radius.xl)
)

@Composable
private fun animateColor(target: Color): Color {
    return animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = AuraTokens.Motion.MediumMs, easing = FastOutSlowInEasing),
        label = "themeColorTransition"
    ).value
}

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val animatedScheme = ColorScheme(
        primary = animateColor(baseScheme.primary),
        onPrimary = animateColor(baseScheme.onPrimary),
        primaryContainer = animateColor(baseScheme.primaryContainer),
        onPrimaryContainer = animateColor(baseScheme.onPrimaryContainer),
        inversePrimary = animateColor(baseScheme.inversePrimary),
        secondary = animateColor(baseScheme.secondary),
        onSecondary = animateColor(baseScheme.onSecondary),
        secondaryContainer = animateColor(baseScheme.secondaryContainer),
        onSecondaryContainer = animateColor(baseScheme.onSecondaryContainer),
        tertiary = animateColor(baseScheme.tertiary),
        onTertiary = animateColor(baseScheme.onTertiary),
        tertiaryContainer = animateColor(baseScheme.tertiaryContainer),
        onTertiaryContainer = animateColor(baseScheme.onTertiaryContainer),
        background = animateColor(baseScheme.background),
        onBackground = animateColor(baseScheme.onBackground),
        surface = animateColor(baseScheme.surface),
        onSurface = animateColor(baseScheme.onSurface),
        surfaceVariant = animateColor(baseScheme.surfaceVariant),
        onSurfaceVariant = animateColor(baseScheme.onSurfaceVariant),
        surfaceTint = animateColor(baseScheme.surfaceTint),
        inverseSurface = animateColor(baseScheme.inverseSurface),
        inverseOnSurface = animateColor(baseScheme.inverseOnSurface),
        error = animateColor(baseScheme.error),
        onError = animateColor(baseScheme.onError),
        errorContainer = animateColor(baseScheme.errorContainer),
        onErrorContainer = animateColor(baseScheme.onErrorContainer),
        outline = animateColor(baseScheme.outline),
        outlineVariant = animateColor(baseScheme.outlineVariant),
        scrim = baseScheme.scrim,
        surfaceBright = animateColor(baseScheme.surfaceBright),
        surfaceDim = animateColor(baseScheme.surfaceDim),
        surfaceContainer = animateColor(baseScheme.surfaceContainer),
        surfaceContainerHigh = animateColor(baseScheme.surfaceContainerHigh),
        surfaceContainerHighest = animateColor(baseScheme.surfaceContainerHighest),
        surfaceContainerLow = animateColor(baseScheme.surfaceContainerLow),
        surfaceContainerLowest = animateColor(baseScheme.surfaceContainerLowest)
    )

    val extendedColors = AuraExtendedColors(
        isDark = darkTheme,
        backgroundPrimary = animateColor(if (darkTheme) NearBlackBackground else WarmWhiteBackground),
        surfaceElevated = animateColor(if (darkTheme) DarkSurfaceElevated else WarmSurfaceElevated),
        surfaceSubtle = animateColor(if (darkTheme) DarkSurfaceSubtle else WarmSurfaceSubtle),
        borderHairline = animateColor(if (darkTheme) DarkBorderHairline else WarmBorderHairline),
        inkPrimary = animateColor(if (darkTheme) PrimaryInkDark else PrimaryInkLight),
        inkSecondary = animateColor(if (darkTheme) SecondaryInkDark else SecondaryInkLight),
        inkTertiary = animateColor(if (darkTheme) TertiaryInkDark else TertiaryInkLight),
        primaryIndigo = animateColor(if (darkTheme) DeepIndigoDark else DeepIndigoLight),
        accentEmerald = animateColor(if (darkTheme) EmeraldAccentDark else EmeraldAccentLight),
        successGreen = animateColor(if (darkTheme) SoftGreenDark else SoftGreenSuccess),
        warningAmber = animateColor(if (darkTheme) AmberWarningDark else AmberWarning),
        dangerRed = animateColor(if (darkTheme) MutedRedDangerDark else MutedRedDanger),
        glassBackground = animateColor(
            if (darkTheme) DarkSurfaceElevated.copy(alpha = 0.88f)
            else WarmSurfaceElevated.copy(alpha = 0.90f)
        )
    )

    CompositionLocalProvider(LocalAuraColors provides extendedColors) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            shapes = AuraShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AuraTheme(darkTheme = darkTheme, content = content)
}
