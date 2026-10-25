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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.CallerAvatar
import com.example.ui.components.EchoLogo
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk

@Composable
fun OnboardingScreen(
    onComplete: (callerName: String, pfpUri: String?, presetId: Int, callBgHex: String) -> Unit
) {
    var callerName by remember { mutableStateOf("Future Me") }
    var selectedPfpUri by remember { mutableStateOf<String?>(null) }
    var selectedPresetId by remember { mutableIntStateOf(1) }
    var selectedCallBg by remember { mutableStateOf("#1F1C1C") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPfpUri = uri.toString()
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EchoLogo(iconSize = 48.dp, textSize = 28.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Set Up Default Caller Profile",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = BeigeYellow
            )

            Text(
                text = "When your future calls you, this is who will show up on your screen.",
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = OffWhite.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Current Avatar Preview
            CallerAvatar(
                pfpUri = selectedPfpUri,
                presetId = selectedPresetId,
                size = 96.dp,
                borderColor = MintBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presets row + Gallery button
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (id in 1..4) {
                    val isSelected = selectedPfpUri == null && selectedPresetId == id
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
                                selectedPfpUri = null
                                selectedPresetId = id
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CallerAvatar(
                            pfpUri = null,
                            presetId = id,
                            size = 40.dp,
                            borderColor = Color.Transparent
                        )
                    }
                }

                // Pick from gallery button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .border(
                            width = if (selectedPfpUri != null) 3.dp else 1.dp,
                            color = if (selectedPfpUri != null) MintBlue else DarkCardBorder,
                            shape = CircleShape
                        )
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Pick custom photo",
                        tint = if (selectedPfpUri != null) MintBlue else OffWhite.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caller Name input
            OutlinedTextField(
                value = callerName,
                onValueChange = { callerName = it },
                label = { Text("Default Caller Name", color = OffWhite.copy(alpha = 0.8f)) },
                placeholder = { Text("e.g. Future Me, 2030 Me", color = OffWhite.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_caller_name_input"),
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

            // Choose Call Background
            Text(
                text = "Choose Call Screen Background",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = BeigeYellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bgColors.forEach { (hex, name) ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedCallBg == hex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCallBg = hex }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MintBlue else DarkCardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                androidx.compose.material3.Icon(
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

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    onComplete(
                        if (callerName.isBlank()) "Future Me" else callerName,
                        selectedPfpUri,
                        selectedPresetId,
                        selectedCallBg
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBlue,
                    contentColor = PremiumBlack
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Begin Echoing",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
