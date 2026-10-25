package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.MintBlue
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isRecordingOrPlaying: Boolean = false,
    amplitude: Float = 0f,
    barCount: Int = 24,
    barWidth: Dp = 4.dp,
    maxHeight: Dp = 56.dp,
    activeColor: Color = MintBlue,
    accentColor: Color = BeigeYellow
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val fraction = if (isRecordingOrPlaying) {
                // Combine normalized amplitude with wave phase
                val offset = (i.toFloat() / barCount)
                val wave = kotlin.math.sin((pulsePhase + offset) * 2 * kotlin.math.PI).toFloat().coerceIn(-1f, 1f)
                val dynamicFactor = if (amplitude > 0.05f) amplitude * 1.5f else 0.25f
                val h = 0.15f + (0.85f * dynamicFactor * ((wave + 1f) / 2f))
                h.coerceIn(0.12f, 1f)
            } else {
                0.12f
            }

            val animHeight = remember { Animatable(fraction) }
            LaunchedEffect(fraction) {
                animHeight.animateTo(
                    targetValue = fraction,
                    animationSpec = tween(durationMillis = 90)
                )
            }

            val color = if (i % 4 == 0) accentColor else activeColor

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(animHeight.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
