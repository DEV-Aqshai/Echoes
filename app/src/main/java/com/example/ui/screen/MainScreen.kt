package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EchoCall
import com.example.ui.components.EchoLogo
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCard
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    calls: List<EchoCall>,
    onNewCallClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreditsClick: () -> Unit,
    onEditCallClick: (EchoCall) -> Unit,
    onDeleteCallClick: (EchoCall) -> Unit,
    onSimulateCallClick: (EchoCall) -> Unit
) {
    var callToDelete by remember { mutableStateOf<EchoCall?>(null) }
    var callToPreview by remember { mutableStateOf<EchoCall?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header Row matching main_screen_ref.png: Logo on left, "Credits" on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EchoLogo(
                    iconSize = 38.dp,
                    textSize = 24.sp
                )

                Text(
                    text = "Credits",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OffWhite,
                    modifier = Modifier
                        .clickable { onCreditsClick() }
                        .padding(8.dp)
                        .testTag("credits_button")
                )
            }

            // Action Row matching main_screen_ref.png: Settings gear on left, Plus on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button with teal background & subtle glow
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), ambientColor = MintBlue, spotColor = MintBlue)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintBlue)
                        .clickable { onSettingsClick() }
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Edit Profile & Settings",
                        tint = PremiumBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Add button with teal background & subtle glow
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), ambientColor = MintBlue, spotColor = MintBlue)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintBlue)
                        .clickable { onNewCallClick() }
                        .testTag("new_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Call",
                        tint = PremiumBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Calls list or empty state
            if (calls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(DarkCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = null,
                                tint = MintBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Echoes Scheduled",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BeigeYellow
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Record your voice and schedule a future call to reach yourself across time.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = OffWhite.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(items = calls, key = { it.id }) { call ->
                        EchoCallCard(
                            call = call,
                            onEdit = { onEditCallClick(call) },
                            onDelete = { callToDelete = call },
                            onTestCall = { onSimulateCallClick(call) }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    callToDelete?.let { call ->
        AlertDialog(
            onDismissRequest = { callToDelete = null },
            title = {
                Text(
                    text = "Delete Echo Call?",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = BeigeYellow
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel the scheduled call \"${call.title}\"? The voice recording will be permanently removed.",
                    fontFamily = SpaceGrotesk,
                    color = OffWhite
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCallClick(call)
                        callToDelete = null
                    }
                ) {
                    Text("Delete", color = DangerRed, fontWeight = FontWeight.Bold, fontFamily = SpaceGrotesk)
                }
            },
            dismissButton = {
                TextButton(onClick = { callToDelete = null }) {
                    Text("Cancel", color = OffWhite, fontFamily = SpaceGrotesk)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun EchoCallCard(
    call: EchoCall,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestCall: () -> Unit
) {
    val formattedTime = formatTimestamp(call.scheduledTimestamp)
    val recurrenceTag = when (call.repeatType) {
        EchoCall.REPEAT_DAILY -> " • Everyday"
        EchoCall.REPEAT_WEEKLY -> " • Weekly"
        EchoCall.REPEAT_MONTHLY -> " • Monthly"
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onTestCall() }
            .testTag("call_card_${call.id}"),
        colors = CardDefaults.cardColors(
            containerColor = BeigeYellow
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = call.title,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PremiumBlack,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$formattedTime$recurrenceTag",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = PremiumBlack.copy(alpha = 0.85f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quick Call Simulation icon
                IconButton(
                    onClick = onTestCall,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Test Call Now",
                        tint = PremiumBlack.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Edit Pencil Icon
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Call",
                        tint = PremiumBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Trash Can Icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Call",
                        tint = DangerRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd - h.mma", Locale.US)
    return sdf.format(Date(timestamp)).lowercase()
}
