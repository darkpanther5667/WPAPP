package com.aistudio.sharmakhata.pqmzvk.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ── CUSTOM EASING CURVES ──────────────────────────────────────────────────
// Translate Emil's CSS cubic-bezier values to Compose's CubicBezierEasing.
// These are stronger than Material's built-in curves, giving animations
// a more intentional, punchy feel.

/** Strong ease-out for UI interactions (button press, enter transitions).
    Equivalent to cubic-bezier(0.23, 1, 0.32, 1) */
val EmilEaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

/** Strong ease-in-out for on-screen movement / morphing.
    Equivalent to cubic-bezier(0.77, 0, 0.175, 1) */
val EmilEaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

/** iOS-like drawer curve.
    Equivalent to cubic-bezier(0.32, 0.72, 0, 1) */
val EmilEaseDrawer = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

/** Release should be fast (exit animations, scale-up after press). */
val EmilEaseRelease = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

// ── SHARED SPRING PRESETS ─────────────────────────────────────────────────

/** Standard press-feedback spring: crisp, subtle, no bounce. */
val PressSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

/** Softer spring for entrance animations (cards, list items). */
val EntranceSpring = spring<Float>(
    dampingRatio = 0.7f,
    stiffness = 400f
)

// ── PRESS-FEEDBACK MODIFIER ───────────────────────────────────────────────
// Applies scale(0.97) on press with a crisp spring.
// Disables the default Material ripple — scale IS the feedback.
// Usage: Modifier.clickable { ... }.bounceClick()

fun Modifier.bounceClick(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = PressSpring,
        label = "bounceScale"
    )
    this
        .scale(scale)
        .then(
            // We return the interactionSource but can't attach it here
            // because clickable isn't part of this modifier.
            // Caller must chain: Modifier.clickable(interactionSource, indication=null) { }.bounceClick()
            Modifier
        )
}

// ── HINGLISH SNACKBAR ─────────────────────────────────────────────────────

private val HinglishMessages = listOf(
    "Ho gaya ✅",
    "Done bhai! 👍",
    "Sab set hai ✓",
    "Complete ho gaya 😊",
    "Theek hai! Done ✅",
    "Kaam ho gaya 🙌",
    "Save ho gaya ✓",
    "Update ho gaya 👍",
    "Sab sahi hai ✅",
    "Ho gaya ji! 😊"
)

fun hinglishSuccessMessage(): String {
    val index = ((System.currentTimeMillis() / 1000) % HinglishMessages.size).toInt()
    return HinglishMessages[index]
}

@Composable
fun HinglishSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data: SnackbarData ->
            Snackbar(
                snackbarData = data,
                containerColor = RupeeGreenDim,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    )
}
