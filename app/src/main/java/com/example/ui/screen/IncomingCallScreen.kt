package com.example.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EchoCall
import com.example.data.repository.UserProfile
import com.example.ui.components.CallerAvatar
import com.example.ui.theme.AnswerGreen
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DeclineRed
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk

@Composable
fun IncomingCallScreen(
    call: EchoCall,
    defaultProfile: UserProfile,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val callerName = call.customCallerName?.ifEmpty { null } ?: defaultProfile.callerName
    val pfpUri = call.customCallerPfpUri ?: defaultProfile.callerPfpUri
    val bgHex = call.callBgHex ?: defaultProfile.callBgHex
    val bgColor = try {
        Color(android.graphics.Color.parseColor(bgHex))
    } catch (e: Exception) {
        PremiumBlack
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Incoming Call Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "INCOMING ECHO CALL",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MintBlue,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = callerName,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = BeigeYellow
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = call.title,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = OffWhite.copy(alpha = 0.85f)
                )

                if (call.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = call.description,
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = OffWhite.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }
            }

            // Middle Section: Caller Avatar with pulsing ripple rings
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding ripple rings
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(rippleScale)
                        .alpha(rippleAlpha)
                        .border(3.dp, MintBlue, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(rippleScale * 0.85f)
                        .alpha(rippleAlpha * 0.9f)
                        .border(2.dp, BeigeYellow, CircleShape)
                )

                CallerAvatar(
                    pfpUri = pfpUri,
                    presetId = defaultProfile.presetAvatarId,
                    size = 130.dp,
                    borderColor = MintBlue
                )
            }

            // Bottom Section: Decline and Answer Call Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onDecline,
                        colors = ButtonDefaults.buttonColors(containerColor = DeclineRed),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("decline_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline Call",
                            tint = OffWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decline",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = OffWhite.copy(alpha = 0.8f)
                    )
                }

                // Answer Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onAnswer,
                        colors = ButtonDefaults.buttonColors(containerColor = AnswerGreen),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("answer_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Answer Call",
                            tint = OffWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Answer",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BeigeYellow
                    )
                }
            }
        }
    }
}
