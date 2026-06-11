package com.aistudio.sharmakhata.pqmzvk.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

// === AMOUNT TYPE ENUM ===
enum class GrahbookAmountType {
    RECEIVED, OUTSTANDING, PENDING, NEUTRAL
}

// === AMOUNT TEXT ===
@Composable
fun AmountText(
    amount: Long,           // in paise/smallest unit
    type: GrahbookAmountType,
    size: TextUnit = 17.sp,
    modifier: Modifier = Modifier
) {
    val rupees = amount / 100.0
    val formatted = "₹${if (rupees == rupees.toLong().toDouble()) {
        NumberFormat.getNumberInstance(Locale("en", "IN")).format(rupees.toLong())
    } else {
        NumberFormat.getNumberInstance(Locale("en", "IN")).format(rupees)
    }}"
    val color = when (type) {
        GrahbookAmountType.RECEIVED    -> RupeeGreen
        GrahbookAmountType.OUTSTANDING -> DebtRed
        GrahbookAmountType.PENDING     -> PendingAmber
        GrahbookAmountType.NEUTRAL     -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = formatted,
        style = TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = size,
            color = color
        ),
        modifier = modifier
    )
}

// === PRIMARY BUTTON ===
@Composable
fun GrahbookPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.97f else 1f,
        animationSpec = PressSpring,
        label = "button_scale"
    )
    val haptic = LocalHapticFeedback.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryVariant = if (primaryColor == Brand500) Brand700 else Saffron600
    val backgroundBrush = if (enabled) {
        Brush.horizontalGradient(listOf(primaryColor, primaryVariant))
    } else {
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(GrahbookRadius.md))
            .background(backgroundBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// === SECONDARY BUTTON ===
@Composable
fun GrahbookSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_scale"
    )
    val haptic = LocalHapticFeedback.current

    val color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(GrahbookRadius.md))
            .border(
                width = 1.5.dp,
                color = color,
                shape = RoundedCornerShape(GrahbookRadius.md)
            )
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// === DESTRUCTIVE BUTTON ===
@Composable
fun GrahbookDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_scale"
    )
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(GrahbookRadius.md))
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.8f))))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onError,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

// === METRIC CARD ===
@Composable
fun MetricCard(
    label: String,
    amount: Long,   // in paise
    trend: Float?,   // e.g. 12.5f (positive) or -3.2f (negative), null if no trend
    accentColor: Color,
    type: GrahbookAmountType,
    icon: ImageVector,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 150.dp)
            .heightIn(min = 100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(GrahbookRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(GrahbookSpacing.lg),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Icon + Trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon with accent background
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(GrahbookRadius.sm))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (trend != null) {
                    val isPositive = trend >= 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(GrahbookRadius.pill))
                            .background(if (isPositive) RupeeGreen.copy(alpha = 0.12f) else DebtRed.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.1f", trend)}%",
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPositive) RupeeGreen else DebtRed
                            )
                        )
                    }
                }
            }

            // Amount — the hero of the card
            Column {
                AmountText(amount = amount, type = type, size = 22.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

// === CUSTOMER AVATAR ===
@Composable
fun CustomerAvatar(
    name: String,
    outstandingPaise: Long,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val initial = (name.firstOrNull()?.uppercase() ?: "S").toString()
    
    val (bgColor, textColor) = when {
        outstandingPaise == 0L -> Brand600 to Color.White
        outstandingPaise in 1..99999L -> PendingAmber.copy(alpha = 0.2f) to PendingAmber
        else -> DebtRed.copy(alpha = 0.2f) to DebtRed
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(width = 1.5.dp, color = textColor.copy(alpha = 0.3f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.45f).sp,
                color = textColor
            )
        )
    }
}

// === STATUS BADGE ===
enum class GrahbookStatus {
    PAID, UNPAID, PARTIAL, ACTIVE, LOW_STOCK, OVERDUE, CANCELLED
}

@Composable
fun StatusBadge(
    status: GrahbookStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        GrahbookStatus.PAID -> Triple(RupeeGreen.copy(alpha = 0.15f), RupeeGreen, stringResource(R.string.paid))
        GrahbookStatus.UNPAID -> Triple(DebtRed.copy(alpha = 0.15f), DebtRed, stringResource(R.string.unpaid))
        GrahbookStatus.PARTIAL -> Triple(PendingAmber.copy(alpha = 0.15f), PendingAmber, stringResource(R.string.partial_label))
        GrahbookStatus.ACTIVE -> Triple(Brand500.copy(alpha = 0.15f), Brand300, stringResource(R.string.active_label))
        GrahbookStatus.LOW_STOCK -> Triple(PendingAmber.copy(alpha = 0.15f), PendingAmber, stringResource(R.string.low_stock))
        GrahbookStatus.OVERDUE -> Triple(DebtRed.copy(alpha = 0.15f), DebtRed, stringResource(R.string.overdue))
        GrahbookStatus.CANCELLED -> Triple(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), MaterialTheme.colorScheme.onSurfaceVariant, stringResource(R.string.cancelled))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GrahbookRadius.pill))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor
            )
        )
    }
}

// === TEXT FIELD ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrahbookTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isAmountField: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    val isFocused = remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> DebtRed
            isFocused.value -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 200),
        label = "border_color"
    )

    Column(modifier = modifier) {
        // Label
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (error != null) DebtRed else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = GrahbookSpacing.xs)
        )

        // Text Field Container
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(GrahbookRadius.md)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            leadingIcon = if (isAmountField) {
                {
                    Text(
                        text = "₹",
                        style = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            } else null,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            shape = RoundedCornerShape(GrahbookRadius.md)
        )

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = DebtRed,
                modifier = Modifier.padding(top = GrahbookSpacing.xs)
            )
        }
    }
}

// ============================================================
// APP AVATAR (gradient-based avatar with initial letter)
// ============================================================

@Composable
fun AppAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = ComponentSize.avatarMedium,
    colorIndex: Int = 0
) {
    val gradient = AvatarColors[colorIndex % AvatarColors.size]
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors = gradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            style = when {
                size >= ComponentSize.avatarLarge -> MaterialTheme.typography.titleMedium
                size >= ComponentSize.avatarMedium -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.labelMedium
            },
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// INFO ROW (label-value pair for detail screens)
// ============================================================

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            trailing()
        } else {
            Text(
                text = value,
                style = valueStyle,
                color = valueColor,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

// ============================================================
// DIVIDER
// ============================================================

@Composable
fun AppDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}
