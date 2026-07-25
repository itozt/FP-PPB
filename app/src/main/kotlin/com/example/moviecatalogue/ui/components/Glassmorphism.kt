package com.example.moviecatalogue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a glassmorphism style to a component.
 * Note: Real-time background blur (Modifier.blur) behind components 
 * is complex to achieve natively on Android without RenderNode APIs (Android 12+)
 * or specific hacks. We simulate glass by using a translucent background 
 * and a thin, semi-transparent white border. When placed over our [LiquidBackground],
 * it creates an authentic liquid glass feel.
 * 
 * @param cornerRadius The corner radius for the squircle shape.
 * @param backgroundColor The translucent background color.
 * @param strokeWidth The width of the glass reflection border.
 * @param strokeColor The color of the glass reflection border.
 */
fun Modifier.glassMorphism(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.08f),
    strokeWidth: Dp = 1.dp,
    strokeColor: Color = Color.White.copy(alpha = 0.15f),
    shape: Shape = RoundedCornerShape(cornerRadius)
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(strokeWidth, strokeColor, shape)
