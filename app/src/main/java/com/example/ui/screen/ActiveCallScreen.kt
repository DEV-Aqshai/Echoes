package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EchoCall
import com.example.data.repository.UserProfile
import com.example.ui.components.CallerAvatar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AnswerGreen
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DeclineRed
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk
import com.example.util.AudioPlayer
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ActiveCallScreen(
    call: EchoCall,
    defaultProfile: UserProfile,
    audioPlayer: AudioPlayer,
    onEndCall: () -> Unit
) {
    val callerName = call.customCallerName?.ifEmpty { null } ?: defaultProfile.callerName
    val pfpUri = call.customCallerPfpUri ?: defaultProfile.callerPfpUri
    val bgHex = call.callBgHex ?: defaultProfile.callBgHex
    val bgColor = try {
        Color(android.graphics.Color.parseColor(bgHex))
    } catch (e: Exception) {
        PremiumBlack
    }

    var callDurationSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentPosition by audioPlayer.currentPosition.collectAsState()
    val totalDuration by audioPlayer.totalDuration.collectAsState()
    val isSpeakerOn by audioPlayer.isSpeakerOn.collectAsState()

    // Automatic in-call timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    // AUTOMATIC AUDIO PLAYBACK when call is accepted!
    LaunchedEffect(call.audioFilePath) {
        if (call.audioFilePath.isNotEmpty()) {
            delay(300) // Brief natural telephony connect pause
            audioPlayer.play(call.audioFilePath)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        bgColor,
                        PremiumBlack,
                        Color(0xFF141212)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Call Status & Contact Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AnswerGreen)
                    )
                    Text(
                        text = "ECHOES VOICE CALL",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MintBlue,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = callerName,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = BeigeYellow
                )

                Spacer(modifier = Modifier.height(6.dp))

                val durationText = String.format(
                    Locale.US,
                    "%02d:%02d",
                    callDurationSeconds / 60,
                    callDurationSeconds % 60
                )
                Text(
                    text = durationText,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = OffWhite.copy(alpha = 0.9f)
                )

                if (call.title.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = call.title,
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = OffWhite.copy(alpha = 0.65f)
                    )
                }
            }

            // Middle Section: Caller Avatar & Playing Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CallerAvatar(
                    pfpUri = pfpUri,
                    presetId = defaultProfile.presetAvatarId,
                    size = 140.dp,
                    borderColor = MintBlue
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Voice Waveform animating automatically to the playing audio
                WaveformVisualizer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    isRecordingOrPlaying = isPlaying,
                    amplitude = if (isPlaying) 0.85f else 0.15f,
                    activeColor = MintBlue,
                    accentColor = BeigeYellow
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Audio playback timeline progress
                val progress = if (totalDuration > 0) {
                    (currentPosition.toFloat() / totalDuration).coerceIn(0f, 1f)
                } else 0f

                Slider(
                    value = progress,
                    onValueChange = { newFrac ->
                        audioPlayer.seekTo((newFrac * totalDuration).toInt())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = BeigeYellow,
                        activeTrackColor = MintBlue,
                        inactiveTrackColor = DarkCardBorder
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val posSec = currentPosition / 1000
                    val totSec = totalDuration / 1000
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", posSec / 60, posSec % 60),
                        fontSize = 12.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isPlaying) "Playing from future" else if (totalDuration > 0) "Finished" else "Connecting...",
                        fontSize = 11.sp,
                        fontFamily = SpaceGrotesk,
                        color = if (isPlaying) MintBlue else BeigeYellow
                    )
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", totSec / 60, totSec % 60),
                        fontSize = 12.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ONLY REPLAY BUTTON (No play/pause button per user request!)
                Button(
                    onClick = {
                        audioPlayer.replay(call.audioFilePath)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkCard,
                        contentColor = BeigeYellow
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MintBlue.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("replay_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay Audio",
                        tint = BeigeYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Replay Message",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BeigeYellow
                    )
                }
            }

            // Bottom Section: In-Call Telephony Action Bar & Big Red End Call
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Secondary Controls Row: Mute, Speaker, Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speakerphone Toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { audioPlayer.toggleSpeaker() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) MintBlue else DarkCard)
                                .border(1.dp, if (isSpeakerOn) MintBlue else DarkCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) PremiumBlack else OffWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSpeakerOn) "Speaker" else "Earpiece",
                            fontSize = 11.sp,
                            fontFamily = SpaceGrotesk,
                            color = OffWhite.copy(alpha = 0.8f)
                        )
                    }

                    // Mute Mic Toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) BeigeYellow else DarkCard)
                                .border(1.dp, if (isMuted) BeigeYellow else DarkCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) PremiumBlack else OffWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isMuted) "Muted" else "Mute",
                            fontSize = 11.sp,
                            fontFamily = SpaceGrotesk,
                            color = OffWhite.copy(alpha = 0.8f)
                        )
                    }

                    // Call Note / Reminder
                    if (call.description.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { showNoteDialog = !showNoteDialog },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (showNoteDialog) MintBlue else DarkCard)
                                    .border(1.dp, DarkCardBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View Note",
                                    tint = if (showNoteDialog) PremiumBlack else OffWhite,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Note",
                                fontSize = 11.sp,
                                fontFamily = SpaceGrotesk,
                                color = OffWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Note Dialog Card
                if (showNoteDialog && call.description.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Note from the past:",
                                fontSize = 11.sp,
                                fontFamily = SpaceGrotesk,
                                color = MintBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = call.description,
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = BeigeYellow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Large Red Phone Hangup Button
                Button(
                    onClick = onEndCall,
                    colors = ButtonDefaults.buttonColors(containerColor = DeclineRed),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = OffWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "End Call",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = OffWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}
