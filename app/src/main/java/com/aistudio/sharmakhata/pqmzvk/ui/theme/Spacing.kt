package com.aistudio.sharmakhata.pqmzvk.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Compatibility aliases — delegates to [GrahbookSpacing].
 * Prefer GrahbookSpacing in new code.
 */
@Deprecated("Use GrahbookSpacing", ReplaceWith("GrahbookSpacing"))
object Spacing {
    val xxsmall get() = GrahbookSpacing.xxsmall
    val unit get() = GrahbookSpacing.xs
    val xsmall get() = GrahbookSpacing.xs
    val small get() = GrahbookSpacing.sm
    val medium get() = GrahbookSpacing.md
    val large get() = GrahbookSpacing.lg
    val xlarge get() = GrahbookSpacing.xl
    val xxlarge get() = GrahbookSpacing.xxl
    val xxxlarge get() = GrahbookSpacing.xxxl
    val huge get() = GrahbookSpacing.huge
    val gigantic get() = GrahbookSpacing.gigantic
    val sectionGap get() = GrahbookSpacing.xl
    val screenPadding get() = GrahbookSpacing.lg
    val cardPadding get() = GrahbookSpacing.lg
    val gutter get() = GrahbookSpacing.md
    val elementGap get() = GrahbookSpacing.sm
    val listItemGap get() = GrahbookSpacing.sm
}

// ============================================================
// ICON SIZES
// ============================================================
object IconSize {
    val xsmall = 14.dp
    val small = 18.dp
    val medium = 24.dp
    val large = 32.dp
    val xlarge = 40.dp
    val xxlarge = 48.dp
    val huge = 56.dp
}

// ============================================================
// ELEVATION
// ============================================================
object Elevation {
    val none = 0.dp
    val flat = 1.dp
    val low = 2.dp
    val medium = 4.dp
    val high = 6.dp
    val highest = 8.dp
    val pressed = 12.dp
}

// ============================================================
// COMPONENT DIMENSIONS
// ============================================================
object ComponentSize {
    val avatarSmall = 36.dp
    val avatarMedium = 44.dp
    val avatarLarge = 52.dp

    val buttonHeight = 48.dp
    val buttonHeightSmall = 40.dp
    val buttonHeightLarge = 56.dp

    val textFieldHeight = 56.dp

    val topBarHeight = 56.dp
    val bottomNavHeight = 80.dp
    val fabSize = 56.dp

    val statCardMinHeight = 80.dp

    val iconContainerSmall = 32.dp
    val iconContainerMedium = 40.dp
    val iconContainerLarge = 48.dp
}
