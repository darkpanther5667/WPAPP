package com.aistudio.sharmakhata.pqmzvk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.ui.theme.Brand500
import com.aistudio.sharmakhata.pqmzvk.ui.theme.Brand600
import com.aistudio.sharmakhata.pqmzvk.ui.theme.GrahbookRadius
import com.aistudio.sharmakhata.pqmzvk.ui.theme.StitchBorder
import com.aistudio.sharmakhata.pqmzvk.ui.theme.StitchSurface
import com.aistudio.sharmakhata.pqmzvk.ui.theme.StitchTextSecondary

/**
 * Shared filter chip component used across Bills, Customers, Items, and other screens.
 *
 * @param selected Whether this chip is currently selected
 * @param onClick Called when the chip is tapped
 * @param text Label text displayed on the chip
 * @param modifier Optional modifier
 */
@Composable
fun FilterChipItem(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        StitchSurface
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        StitchTextSecondary
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        StitchBorder
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GrahbookRadius.pill))
            .background(containerColor)
            .border(width = 0.5.dp, color = borderColor, shape = RoundedCornerShape(GrahbookRadius.pill))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
