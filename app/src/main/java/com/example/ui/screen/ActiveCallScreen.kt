package com.example.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.ui.theme.DeclineRed
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
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

    // Call timer ticker
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
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
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Call Status & Caller Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AnswerGreen)
                    )
                    Text(
                        text = "CONNECTED ACROSS TIME",
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
                    fontSize = 28.sp,
                    color = BeigeYellow
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                    fontSize = 16.sp,
                    color = OffWhite.copy(alpha = 0.8f)
                )
            }

            // Middle Section: Caller Avatar & Voice Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CallerAvatar(
                    pfpUri = pfpUri,
                    presetId = defaultProfile.presetAvatarId,
                    size = 130.dp,
                    borderColor = MintBlue
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Voice Waveform visualizer reacting to playback
                WaveformVisualizer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    isRecordingOrPlaying = isPlaying,
                    amplitude = if (isPlaying) 0.85f else 0.1f,
                    activeColor = MintBlue,
                    accentColor = BeigeYellow
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Audio playback progress slider
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
                        .padding(horizontal = 32.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = BeigeYellow,
                        activeTrackColor = MintBlue,
                        inactiveTrackColor = DarkCardBorder
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val posSec = currentPosition / 1000
                    val totSec = totalDuration / 1000
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", posSec / 60, posSec % 60),
                        fontSize = 11.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", totSec / 60, totSec % 60),
                        fontSize = 11.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.6f)
                    )
                }

                // Audio Play/Pause or Replay controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                audioPlayer.pause()
                            } else {
                                if (currentPosition >= totalDuration && totalDuration > 0) {
                                    audioPlayer.play(call.audioFilePath)
                                } else {
                                    audioPlayer.resume()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Echo" else "Play Echo",
                            tint = MintBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            audioPlayer.play(call.audioFilePath)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Restart Echo",
                            tint = BeigeYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Section: Call Action Buttons & End Call
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Secondary Controls Row: Mute, Speaker, Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) BeigeYellow else DarkCard)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) PremiumBlack else OffWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isMuted) "Muted" else "Mute",
                            fontSize = 11.sp,
                            fontFamily = SpaceGrotesk,
                            color = OffWhite.copy(alpha = 0.7f)
                        )
                    }

                    // Speakerphone Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { audioPlayer.toggleSpeaker() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) MintBlue else DarkCard)
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) PremiumBlack else OffWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSpeakerOn) "Speaker" else "Earpiece",
                            fontSize = 11.sp,
                            fontFamily = SpaceGrotesk,
                            color = OffWhite.copy(alpha = 0.7f)
                        )
                    }

                    // View Note Button
                    if (call.description.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { showNoteDialog = !showNoteDialog },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(DarkCard)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View Note",
                                    tint = OffWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Note",
                                fontSize = 11.sp,
                                fontFamily = SpaceGrotesk,
                                color = OffWhite.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                if (showNoteDialog && call.description.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = call.description,
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = BeigeYellow,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // End Call Button
                Button(
                    onClick = onEndCall,
                    colors = ButtonDefaults.buttonColors(containerColor = DeclineRed),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(74.dp)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = OffWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "End Echo Call",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = OffWhite.copy(alpha = 0.6f)
                )
            }
        }
    }
}
