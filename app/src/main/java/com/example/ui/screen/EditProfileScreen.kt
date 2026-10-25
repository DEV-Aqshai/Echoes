package com.example.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.data.repository.UserProfile
import com.example.ui.components.CallerAvatar
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk
import com.example.util.RingtonePlayer

@Composable
fun EditProfileScreen(
    currentProfile: UserProfile,
    ringtonePlayer: RingtonePlayer,
    onBack: () -> Unit,
    onSave: (
        callerName: String,
        callerPfpUri: String?,
        presetAvatarId: Int,
        callBgHex: String,
        ringtoneId: String
    ) -> Unit
) {
    var callerName by remember { mutableStateOf(currentProfile.callerName) }
    var pfpUri by remember { mutableStateOf(currentProfile.callerPfpUri) }
    var presetId by remember { mutableIntStateOf(currentProfile.presetAvatarId) }
    var callBgHex by remember { mutableStateOf(currentProfile.callBgHex) }
    var ringtoneId by remember { mutableStateOf(currentProfile.ringtoneId) }
    var isPreviewingRingtone by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pfpUri = uri.toString()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ringtonePlayer.stopRinging()
        }
    }

    val bgColors = listOf(
        Pair("#1F1C1C", "Premium Black"),
        Pair("#0D2229", "Deep Teal"),
        Pair("#22132B", "Cosmic Violet"),
        Pair("#151B24", "Midnight Navy")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        ringtonePlayer.stopRinging()
                        onBack()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OffWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Default Caller Profile",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = BeigeYellow
                )
            }

            // PFP Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CallerAvatar(
                        pfpUri = pfpUri,
                        presetId = presetId,
                        size = 96.dp,
                        borderColor = MintBlue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Caller Avatar",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BeigeYellow
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (id in 1..4) {
                            val isSelected = pfpUri == null && presetId == id
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) BeigeYellow else DarkCardBorder,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        pfpUri = null
                                        presetId = id
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                CallerAvatar(pfpUri = null, presetId = id, size = 40.dp, borderColor = Color.Transparent)
                            }
                        }

                        // Upload button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkCardBorder)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Photo",
                                tint = MintBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (pfpUri != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { pfpUri = null },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove Custom Photo", fontSize = 12.sp, fontFamily = SpaceGrotesk)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Caller Name
            Text(
                text = "Default Caller Name",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = callerName,
                onValueChange = { callerName = it },
                label = { Text("Caller Name", color = OffWhite.copy(alpha = 0.8f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_caller_name_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintBlue,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Call Background Theme
            Text(
                text = "Call Screen Background Theme",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bgColors.forEach { (hex, name) ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = callBgHex == hex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { callBgHex = hex }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MintBlue else DarkCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MintBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            color = if (isSelected) MintBlue else OffWhite.copy(alpha = 0.6f),
                            fontFamily = SpaceGrotesk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ringtone Section
            Text(
                text = "Default Ringtone",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("echo_pulse", "Echo Pulse (Futuristic Sonar)"),
                    Pair("retro_phone", "Retro Phone (Classic Bell)"),
                    Pair("cosmic_call", "Cosmic Call (Ascending Chime)")
                ).forEach { (id, name) ->
                    val isSelected = ringtoneId == id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                ringtoneId = id
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkCardBorder else DarkCard
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MintBlue else DarkCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BeigeYellow else OffWhite,
                                fontSize = 14.sp
                            )

                            IconButton(
                                onClick = {
                                    if (isPreviewingRingtone) {
                                        ringtonePlayer.stopRinging()
                                        isPreviewingRingtone = false
                                    } else {
                                        ringtonePlayer.previewRingtone(id)
                                        isPreviewingRingtone = true
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPreviewingRingtone && ringtoneId == id) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Test Audio",
                                    tint = MintBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    ringtonePlayer.stopRinging()
                    onSave(
                        if (callerName.isBlank()) "Future Me" else callerName.trim(),
                        pfpUri,
                        presetId,
                        callBgHex,
                        ringtoneId
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBlue,
                    contentColor = PremiumBlack
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save Profile",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
