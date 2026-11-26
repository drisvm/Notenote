package com.notenote.app.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

val NotenoteTypography = Typography().let { base ->
    base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    )
}

// Roomy, stationery-like rounding with a soft radius scale.
private val PaperSmall = RoundedCornerShape(10.dp)
private val PaperMedium = RoundedCornerShape(16.dp)
private val PaperLarge = RoundedCornerShape(24.dp)

val NotenoteShapes = Shapes(
    extraSmall = PaperSmall,
    small = PaperSmall,
    medium = PaperMedium,
    large = PaperLarge,
    extraLarge = PaperLarge,
)
