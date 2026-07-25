package com.example.moviecatalogue.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A beautiful modern liquid background with slow moving blobs.
 */
@Composable
fun LiquidBackground(modifier: Modifier = Modifier) {
    // Colors based on user preference: Netflix Red & Black
    val bgColor = Color(0xFF050505) // Deep black
    val blobColor1 = Color(0xFFE50914).copy(alpha = 0.4f) // Netflix Red
    val blobColor2 = Color(0xFF8B0000).copy(alpha = 0.5f) // Dark Red

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Deep black, no red lights
    )
}
