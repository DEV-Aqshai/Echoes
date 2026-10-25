package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DarkCard
import com.example.ui.theme.MintBlue

@Composable
fun CallerAvatar(
    modifier: Modifier = Modifier,
    pfpUri: String? = null,
    presetId: Int = 1,
    size: Dp = 64.dp,
    borderColor: Color = MintBlue
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(DarkCard),
        contentAlignment = Alignment.Center
    ) {
        if (!pfpUri.isNullOrEmpty()) {
            AsyncImage(
                model = Uri.parse(pfpUri),
                contentDescription = "Caller Profile Picture",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Stylized Preset Avatars
            val (gradient, icon) = when (presetId) {
                1 -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF1B3B4B), Color(0xFF4C9DB0))),
                    Icons.Default.HourglassTop
                )
                2 -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF4D3817), Color(0xFFFFEBAF))),
                    Icons.Default.AutoAwesome
                )
                3 -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF381B4B), Color(0xFF9C4CB0))),
                    Icons.Default.Psychology
                )
                4 -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF1B4B32), Color(0xFF4CB072))),
                    Icons.Default.RecordVoiceOver
                )
                else -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF2B2B2B), Color(0xFF5C5C5C))),
                    Icons.Default.Person
                )
            }

            Box(
                modifier = Modifier
                    .size(size)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Preset Avatar",
                    tint = BeigeYellow,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}
