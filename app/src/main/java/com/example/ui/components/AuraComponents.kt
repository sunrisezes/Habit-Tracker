package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AuraNumericTypography
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable Icon Catalog mapping icon string keys to crisp Material Symbols.
 */
object AuraIconCatalog {
    val IconsByName: Map<String, ImageVector> = linkedMapOf(
        "meditation" to Icons.Outlined.SelfImprovement,
        "book" to Icons.AutoMirrored.Outlined.MenuBook,
        "water" to Icons.Outlined.WaterDrop,
        "fitness" to Icons.Outlined.FitnessCenter,
        "run" to Icons.AutoMirrored.Outlined.DirectionsRun,
        "journal" to Icons.Outlined.EditNote,
        "focus" to Icons.Outlined.Psychology,
        "sleep" to Icons.Outlined.Bedtime,
        "sun" to Icons.Outlined.WbSunny,
        "code" to Icons.Outlined.Code,
        "nature" to Icons.Outlined.Eco,
        "spark" to Icons.Outlined.AutoAwesome,
        "tea" to Icons.Outlined.LocalCafe,
        "heart" to Icons.Outlined.FavoriteBorder,
        "creative" to Icons.Outlined.Brush,
        "energy" to Icons.Outlined.Bolt,
        "timer" to Icons.Outlined.Timer,
        "spa" to Icons.Outlined.Spa,
        "sound" to Icons.Outlined.GraphicEq
    )

    fun resolve(name: String): ImageVector {
        return IconsByName[name.lowercase()] ?: Icons.Outlined.AutoAwesome
    }
}

/**
 * Reusable Luxury Card with soft elevated surface, hairline border, and subtle press feedback.
 */
@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glassmorphism: Boolean = false,
    cornerRadius: Dp = AuraTokens.Radius.lg,
    borderColor: Color? = null,
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(AuraTokens.Spacing.lg),
    content: @Composable () -> Unit
) {
    val colors = LocalAuraColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = AuraTokens.Motion.springSnappy(),
        label = "cardScale"
    )

    val bg = containerColor ?: if (glassmorphism) {
        colors.glassBackground
    } else {
        colors.surfaceElevated
    }
    val stroke = borderColor ?: colors.borderHairline

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = bg,
        border = BorderStroke(1.dp, stroke),
        tonalElevation = if (glassmorphism) AuraTokens.Elevation.floating else AuraTokens.Elevation.subtle,
        shadowElevation = if (colors.isDark) 0.dp else 2.dp
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Reusable Primary & Secondary Buttons with tactile spring scale.
 */
@Composable
fun AuraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = LocalAuraColors.current.primaryIndigo,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = AuraTokens.Motion.springSnappy(),
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(AuraTokens.Radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .scale(scale)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AuraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val colors = LocalAuraColors.current
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(AuraTokens.Radius.md),
        border = BorderStroke(1.dp, colors.borderHairline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surfaceSubtle.copy(alpha = 0.6f),
            contentColor = colors.inkPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = colors.inkSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Reusable Luxury Input Field
 */
@Composable
fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String? = null
) {
    val colors = LocalAuraColors.current
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkTertiary
                    )
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.inkPrimary),
            shape = RoundedCornerShape(AuraTokens.Radius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceElevated,
                unfocusedContainerColor = colors.surfaceSubtle.copy(alpha = 0.55f),
                focusedBorderColor = colors.primaryIndigo,
                unfocusedBorderColor = colors.borderHairline,
                cursorColor = colors.primaryIndigo
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        )
    }
}

/**
 * Reusable Pill Badge for Category, Priority, Difficulty, and Duration.
 */
@Composable
fun AuraBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalAuraColors.current.primaryIndigo,
    icon: ImageVector? = null,
    filled: Boolean = false
) {
    val bg = if (filled) accentColor.copy(alpha = 0.16f) else accentColor.copy(alpha = 0.09f)
    val border = accentColor.copy(alpha = 0.24f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(AuraTokens.Radius.pill))
            .background(bg)
            .border(0.75.dp, border, RoundedCornerShape(AuraTokens.Radius.pill))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Interactive Progress Ring & Completion Checkbox with spring-animated sweep and particle burst.
 */
@Composable
fun AuraCompletionRingButton(
    completedCount: Int,
    targetCount: Int,
    accentColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    testTag: String? = null
) {
    val colors = LocalAuraColors.current
    val safeTarget = targetCount.coerceAtLeast(1)
    val rawProgress = (completedCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
    val isComplete = completedCount >= safeTarget

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ringProgress"
    )

    val burstAnim = remember { Animatable(0f) }
    LaunchedEffect(isComplete) {
        if (isComplete) {
            burstAnim.snapTo(0.1f)
            burstAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        } else {
            burstAnim.snapTo(0f)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .clip(CircleShape)
            .clickable(
                role = Role.Checkbox,
                onClick = onToggle
            )
            .semantics {
                contentDescription = if (isComplete) {
                    "Completed ($completedCount of $safeTarget)"
                } else {
                    "Progress $completedCount of $safeTarget. Tap to log progress."
                }
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 3.2.dp.toPx()
            val inset = strokeWidth * 1.8f
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // Track ring
            drawArc(
                color = colors.borderHairline,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress arc
            if (animatedProgress > 0.01f) {
                drawArc(
                    color = if (isComplete) colors.accentEmerald else accentColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Filled center when completed
            if (isComplete) {
                drawCircle(
                    color = colors.accentEmerald,
                    radius = (arcSize.width / 2f) - strokeWidth * 0.9f,
                    center = center
                )
            }

            // Subtle luxury particle burst on completion
            val b = burstAnim.value
            if (b > 0.05f && b < 0.98f) {
                val maxRadius = this.size.width / 2f
                val particleRadius = (1f - b) * 2.4.dp.toPx()
                val ringDist = (arcSize.width / 2f) + (b * 6.dp.toPx())
                for (i in 0 until 8) {
                    val angleRad = Math.toRadians((i * 45.0) - 90.0)
                    val px = center.x + (cos(angleRad) * ringDist).toFloat()
                    val py = center.y + (sin(angleRad) * ringDist).toFloat()
                    if (ringDist <= maxRadius) {
                        drawCircle(
                            color = colors.accentEmerald.copy(alpha = 1f - b),
                            radius = particleRadius,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isComplete,
            enter = scaleIn(animationSpec = AuraTokens.Motion.springBouncy()) + fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        if (!isComplete) {
            Text(
                text = if (safeTarget > 1) "$completedCount/$safeTarget" else "",
                style = AuraNumericTypography.SmallBadgeMetric,
                color = colors.inkSecondary
            )
        }
    }
}

/**
 * Large Hero Progress Ring used on Dashboard and Pomodoro Timer.
 */
@Composable
fun AuraHeroProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 128.dp,
    strokeWidth: Dp = 10.dp,
    primaryColor: Color = LocalAuraColors.current.primaryIndigo,
    secondaryColor: Color = LocalAuraColors.current.accentEmerald,
    centerContent: @Composable () -> Unit
) {
    val colors = LocalAuraColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heroRingProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            drawArc(
                color = colors.borderHairline.copy(alpha = 0.65f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (animatedProgress > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor, secondaryColor, primaryColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        centerContent()
    }
}

/**
 * Custom Editorial Minimalist Canvas Art for Empty States & Zen Headers.
 */
@Composable
fun AuraZenIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val colors = LocalAuraColors.current
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Soft orbital rings
        drawCircle(
            color = colors.primaryIndigo.copy(alpha = 0.10f),
            radius = w * 0.44f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        drawCircle(
            color = colors.primaryIndigo.copy(alpha = 0.25f),
            radius = w * 0.36f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Balanced zen stones
        drawOval(
            color = colors.primaryIndigo.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.26f, h * 0.62f),
            size = Size(w * 0.48f, h * 0.16f)
        )
        drawOval(
            color = colors.accentEmerald.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.33f, h * 0.47f),
            size = Size(w * 0.34f, h * 0.14f)
        )
        drawCircle(
            color = colors.warningAmber.copy(alpha = 0.90f),
            radius = w * 0.07f,
            center = Offset(w * 0.5f, h * 0.35f)
        )
    }
}

/**
 * Section Header with Title and optional trailing action.
 */
@Composable
fun AuraSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = LocalAuraColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.inkPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}
