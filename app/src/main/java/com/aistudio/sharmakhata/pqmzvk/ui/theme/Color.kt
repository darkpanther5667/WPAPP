package com.aistudio.sharmakhata.pqmzvk.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// BRAND PRIMARY — WhatsApp Green (Stitch design)
// ============================================================
val WhatsAppPrimary = Color(0xFF25D366)
val WhatsAppPrimaryDark = Color(0xFF1DA851)
val WhatsAppPrimaryLight = Color(0xFF5EE98A)
val PrimaryContainer = Color(0xFF1A3A2A)
val OnPrimaryContainer = Color(0xFFA8F0C0)

// ============================================================
// BRAND SECONDARY — Blue accent
// ============================================================
val AccentBlue = Color(0xFF4A9EFF)
val AccentBlueDark = Color(0xFF2B7DE0)
val AccentBlueContainer = Color(0xFF1A2A40)
val AccentBlueOnContainer = Color(0xFFA8C8FF)

// Keep original Indigo values for backward compatibility
val IndigoPrimary = Color(0xFF4F46E5)
val IndigoLight = Color(0xFF818CF8)
val IndigoDark = Color(0xFF312E81)
val IndigoContainer = Color(0xFFEEF2FF)
val IndigoOnContainer = Color(0xFF3730A3)

// ============================================================
// BRAND SECONDARY — Emerald (for positive / money-in)
// ============================================================
val EmeraldSecondary = Color(0xFF10B981)
val EmeraldLight = Color(0xFF34D399)
val EmeraldDark = Color(0xFF065F46)
val EmeraldContainer = Color(0xFFECFDF5)
val EmeraldOnContainer = Color(0xFF047857)

// ============================================================
// AMBER — Pending / Dues
// ============================================================
val AmberWarning = Color(0xFFF59E0B)
val AmberLight = Color(0xFFFCD34D)
val AmberDark = Color(0xFFB45309)
val AmberContainer = Color(0xFFFFFBEB)
val AmberOnContainer = Color(0xFF92400E)

// ============================================================
// DANGER — Overdue / Error / Money-out
// ============================================================
val ErrorRed = Color(0xFFEF4444)
val ErrorRedLight = Color(0xFFFCA5A5)
val ErrorRedDark = Color(0xFFB91C1C)
val ErrorContainer = Color(0xFFFEF2F2)
val ErrorOnContainer = Color(0xFF991B1B)

val OrangeDanger = Color(0xFFF97316)
val OrangeLight = Color(0xFFFDBA74)
val OrangeDark = Color(0xFFC2410C)

// ============================================================
// SUCCESS — Paid / Received
// ============================================================
val SuccessGreen = Color(0xFF22C55E)
val SuccessGreenLight = Color(0xFF86EFAC)
val SuccessGreenDark = Color(0xFF15803D)

// ============================================================
// WARNING
// ============================================================
val WarningOrange = Color(0xFFF59E0B)

// ============================================================
// NEUTRALS — Slate scale
// ============================================================
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate950 = Color(0xFF020617)

// ============================================================
// FINANCIAL SEMANTIC COLORS
// ============================================================
val AmountDue = ErrorRed                      // Customer owes money
val AmountCredit = SuccessGreen               // You owe customer / money received
val AmountNeutral = Slate500                  // Settled / zero balance
val AmountPending = AmberWarning              // Pending / partial

val BillPaid = SuccessGreen
val BillUnpaid = ErrorRed
val BillPartial = AmberWarning
val BillOverdue = Color(0xFFDC2626)           // Stronger red for overdue

// ============================================================
// STATUS BADGE COLORS
// ============================================================
val BadgePaidBg = Color(0xFFDCFCE7)
val BadgePaidText = Color(0xFF166534)
val BadgeUnpaidBg = Color(0xFFFEE2E2)
val BadgeUnpaidText = Color(0xFF991B1B)
val BadgePartialBg = Color(0xFFFEF3C7)
val BadgePartialText = Color(0xFF92400E)
val BadgeOverdueBg = Color(0xFFFEE2E2)
val BadgeOverdueText = Color(0xFF7F1D1D)

// ============================================================
// WHATSAPP BRAND
// ============================================================
val WhatsAppGreen = Color(0xFF25D366)
val WhatsAppDark = Color(0xFF128C7E)
val WhatsAppLight = Color(0xFFDCF8C6)

// ============================================================
// STITCH DARK SURFACE COLORS (exact from Stitch tailwind config)
// ============================================================
val StitchBg = Color(0xFF10141A)           // surface-dim / background
val StitchSurface = Color(0xFF1C2026)      // surface-container
val StitchSurfaceLow = Color(0xFF181C22)   // surface-container-low
val StitchSurfaceHigh = Color(0xFF262A31)  // surface-container-high
val StitchSurfaceHighest = Color(0xFF31353C) // surface-container-highest / surface-variant
val StitchSurfaceBright = Color(0xFF353940)  // surface-bright
val StitchSurfaceLowest = Color(0xFF0A0E14) // surface-container-lowest
val StitchBorder = Color(0xFF3C4A3D)       // outline-variant
val StitchOutline = Color(0xFF869584)       // outline
val StitchTextPrimary = Color(0xFFDFE2EB)  // on-surface / on-background
val StitchTextSecondary = Color(0xFFBBCBB9) // on-surface-variant
val StitchPrimary = Color(0xFF4FF07F)      // primary (bright green)
val StitchPrimaryContainer = Color(0xFF25D366) // primary-container (WhatsApp green)
val StitchOnPrimary = Color(0xFF003915)    // on-primary
val StitchOnPrimaryContainer = Color(0xFF005523) // on-primary-container

// ============================================================
// DARK THEME SURFACES — aliases
// ============================================================
val BackgroundDark = StitchBg
val SurfaceDark = StitchSurface
val SurfaceVariantDark = StitchSurfaceHighest
val SurfaceHighDark = StitchSurfaceHigh

val TextPrimaryDark = StitchTextPrimary
val TextSecondaryDark = StitchTextSecondary
val TextTertiaryDark = StitchTextSecondary // alias

// ============================================================
// LIGHT THEME SURFACES (keep for light mode support)
// ============================================================
val AppBackground = Color(0xFFF5F5F5)
val BackgroundLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF0F0F0)
val SurfaceHighLight = Color(0xFFFFFFFF)

val TextPrimaryLight = Color(0xFF1C1C1E)
val TextSecondaryLight = Color(0xFF8E8E93)
val TextTertiaryLight = Color(0xFFAEAEB2)
val TextOnPrimaryLight = Color(0xFFFFFFFF)

// ============================================================
// BORDERS & DIVIDERS
// ============================================================
val CardBorder = Color(0xFF30363D)
val DividerColor = StitchSurfaceHighest
val DividerDark = StitchBorder
val OutlineLight = Color(0xFFD1D5DB)
val OutlineDark = StitchBorder

// ============================================================
// STITCH TEAL ACCENT (keep for backward compatibility)
// ============================================================
val StitchTeal = Color(0xFF0D9488)
val StitchTealLight = Color(0xFF14B8A6)
val StitchTealDark = Color(0xFF0F766E)
val StitchTealContainer = Color(0xFFF0FDFA)
val StitchTealOnContainer = Color(0xFF115E59)
val StitchTealBg = Color(0xFFCCFBF1)

// ============================================================
// STITCH SKY BLUE — Secondary accent
// ============================================================
val StitchSky = Color(0xFF0EA5E9)
val StitchSkyLight = Color(0xFF38BDF8)
val StitchSkyDark = Color(0xFF0284C7)

// ============================================================
// GRADIENT PAIRS (for stat cards, avatars, action icons)
// ============================================================
val GradientWhatsApp = listOf(WhatsAppPrimary, WhatsAppPrimaryDark)
val GradientTeal = listOf(StitchTeal, StitchTealDark)
val GradientSky = listOf(StitchSky, StitchSkyDark)
val GradientIndigo = listOf(AccentBlue, AccentBlueDark)
val GradientEmerald = listOf(EmeraldSecondary, EmeraldDark)
val GradientAmber = listOf(AmberWarning, AmberDark)
val GradientOrange = listOf(OrangeDanger, OrangeDark)
val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))

// ============================================================
// AVATAR TINT COLORS (for customer list variety)
// ============================================================
val AvatarColors = listOf(
    listOf(WhatsAppPrimary, WhatsAppPrimaryDark),
    listOf(EmeraldSecondary, EmeraldDark),
    listOf(AmberWarning, AmberDark),
    listOf(OrangeDanger, OrangeDark),
    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
    listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
    listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
    listOf(Color(0xFF84CC16), Color(0xFF4D7C0F)),
    listOf(StitchTeal, StitchTealDark),
    listOf(StitchSky, StitchSkyDark),
)

// ============================================================
// STITCH SECONDARY — Blue
// ============================================================
val StitchSecondary = Color(0xFFA4C9FF)           // secondary (light blue text)
val StitchOnSecondary = Color(0xFF00315D)          // on-secondary
val StitchSecondaryContainer = Color(0xFF3B93F3)  // secondary-container (blue)
val StitchOnSecondaryContainer = Color(0xFF002A52) // on-secondary-container

// ============================================================
// STITCH TERTIARY — Orange/Coral
// ============================================================
val StitchTertiary = Color(0xFFFFC7B6)             // tertiary (light orange text)
val StitchOnTertiary = Color(0xFF5D1900)           // on-tertiary
val StitchTertiaryContainer = Color(0xFFFFA080)    // tertiary-container (orange)
val StitchOnTertiaryContainer = Color(0xFF862800)  // on-tertiary-container

// ============================================================
// STITCH ERROR
// ============================================================
val StitchError = Color(0xFFFFB4AB)
val StitchOnError = Color(0xFF690005)
val StitchErrorContainer = Color(0xFF93000A)

// ============================================================
// BACKWARD COMPATIBILITY ALIASES
// ============================================================
val StitchSurfaceVariant = StitchSurfaceHighest
val StitchTextTertiary = StitchTextSecondary
