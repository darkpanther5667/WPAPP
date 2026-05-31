package com.aistudio.sharmakhata.pqmzvk.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================
// MATERIAL 3 SHAPES — Mapped from Stitch Tailwind config
// ============================================================
// Stitch border-radius mapping:
//   rounded-lg (8px) → buttons, inputs, small containers
//   rounded-xl (12px) → cards, large containers
//   rounded-full (9999px) → pills, badges, avatars
// ============================================================

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),     // Tiny chips
    small = RoundedCornerShape(8.dp),           // Stitch rounded-lg — buttons, inputs
    medium = RoundedCornerShape(12.dp),         // Stitch rounded-xl — cards, containers
    large = RoundedCornerShape(16.dp),          // Larger containers, dialogs
    extraLarge = RoundedCornerShape(24.dp)      // Sheets, modals
)

// ===== NAMED SHAPE TOKENS =====

/** Cards, stat cards — Stitch rounded-xl = 12dp */
val CardShape = RoundedCornerShape(12.dp)

/** Buttons — Stitch rounded-lg = 8dp */
val ButtonShape = RoundedCornerShape(8.dp)

/** Text input fields — Stitch rounded-lg = 8dp */
val TextFieldShape = RoundedCornerShape(8.dp)

/** Status badges, pills — Stitch rounded-full */
val BadgeShape = RoundedCornerShape(50)

/** FAB — Stitch rounded-xl = 12dp */
val FabShape = RoundedCornerShape(12.dp)

/** Bottom sheet */
val BottomSheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

/** List item cards */
val ListCardShape = RoundedCornerShape(12.dp)

/** Dialogs */
val DialogShape = RoundedCornerShape(16.dp)

/** Avatar circles */
val AvatarShape = RoundedCornerShape(50)

/** Action icon containers */
val ActionIconShape = RoundedCornerShape(12.dp)

/** Search bar */
val SearchBarShape = RoundedCornerShape(8.dp)

/** Snackbar */
val SnackbarShape = RoundedCornerShape(8.dp)

/** Top app bar */
val TopBarShape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
