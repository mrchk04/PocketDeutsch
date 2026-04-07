package com.mrchk.pocketdeutsch.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Design-system shape tokens (updated from SVG source of truth).
 *
 * extraSmall  –  8 dp   (label/tag chips, rx=8 in design)
 * small       – 12 dp   (icon buttons, nav items)
 * medium      – 15 dp   (buttons, input fields, rx=15 in design)
 * large       – 24 dp   (cards, progress card, grid tiles, rx=23-24)
 * extraLarge  – 32 dp   (lesson card full modal, rx=32)
 */
val PocketDeutschShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)