package com.example.ui.screen

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.EchoCall
import com.example.data.repository.UserProfile
import com.example.ui.components.CallerAvatar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AnswerGreen
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk
import com.example.util.AudioPlayer
import com.example.util.VoiceRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CreateEditCallScreen(
    initialCall: EchoCall?,
    defaultProfile: UserProfile,
    voiceRecorder: VoiceRecorder,
    audioPlayer: AudioPlayer,
    onBack: () -> Unit,
    onSave: (
        title: String,
        description: String,
        scheduledTimestamp: Long,
        repeatType: String,
        repeatDayOfWeek: Int?,
        audioFilePath: String,
        audioDurationSeconds: Int,
        customCallerName: String?,
        customCallerPfpUri: String?,
        ringtoneId: String,
        callBgHex: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialCall?.title ?: "") }
    var description by remember { mutableStateOf(initialCall?.description ?: "") }

    val defaultScheduled = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 20)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.timeInMillis
    }
    var scheduledTimestamp by remember {
        mutableLongStateOf(initialCall?.scheduledTimestamp ?: defaultScheduled)
    }

    var repeatType by remember {
        mutableStateOf(initialCall?.repeatType ?: EchoCall.REPEAT_ONCE)
    }
    var repeatDayOfWeek by remember {
        mutableStateOf(initialCall?.repeatDayOfWeek)
    }

    var recordedFilePath by remember {
        mutableStateOf(initialCall?.audioFilePath ?: "")
    }
    var recordedDurationSeconds by remember {
        mutableIntStateOf(initialCall?.audioDurationSeconds ?: 0)
    }

    var useCustomCaller by remember {
        mutableStateOf(initialCall?.customCallerName != null || initialCall?.customCallerPfpUri != null)
    }
    var customCallerName by remember {
        mutableStateOf(initialCall?.customCallerName ?: "")
    }
    var customCallerPfpUri by remember {
        mutableStateOf(initialCall?.customCallerPfpUri)
    }
    var selectedPresetId by remember { mutableIntStateOf(1) }

    var selectedRingtone by remember {
        mutableStateOf(initialCall?.ringtoneId ?: defaultProfile.ringtoneId)
    }

    val isRecording by voiceRecorder.isRecording.collectAsState()
    val recordingDuration by voiceRecorder.recordingDurationSeconds.collectAsState()
    val currentAmplitude by voiceRecorder.currentAmplitude.collectAsState()

    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentPosition by audioPlayer.currentPosition.collectAsState()
    val totalDuration by audioPlayer.totalDuration.collectAsState()

    // Permission launcher for microphone
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            voiceRecorder.startRecording()
        } else {
            Toast.makeText(context, "Microphone permission is required to record your echo", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo picker for custom caller PFP
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customCallerPfpUri = uri.toString()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
            if (isRecording) {
                voiceRecorder.stopRecording()
            }
        }
    }

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
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
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
                    text = if (initialCall != null) "Edit Echo Call" else "New Echo Call",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = BeigeYellow
                )
            }

            // SECTION 1: VOICE RECORDING
            Text(
                text = "1. Record Your Message",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                    WaveformVisualizer(
                        modifier = Modifier.fillMaxWidth(),
                        isRecordingOrPlaying = isRecording || isPlaying,
                        amplitude = if (isRecording) currentAmplitude else 0.5f,
                        activeColor = if (isRecording) DangerRed else MintBlue,
                        accentColor = BeigeYellow
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val timeText = if (isRecording) {
                        String.format(Locale.US, "%02d:%02d", recordingDuration / 60, recordingDuration % 60)
                    } else if (isPlaying) {
                        val posSec = currentPosition / 1000
                        String.format(Locale.US, "%02d:%02d", posSec / 60, posSec % 60)
                    } else if (recordedFilePath.isNotEmpty()) {
                        String.format(Locale.US, "%02d:%02d", recordedDurationSeconds / 60, recordedDurationSeconds % 60)
                    } else {
                        "00:00"
                    }

                    Text(
                        text = timeText,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = if (isRecording) DangerRed else OffWhite
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Recording Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRecording) {
                            // Record or Re-record button
                            Button(
                                onClick = {
                                    audioPlayer.stop()
                                    if (hasMicPermission) {
                                        val file = voiceRecorder.startRecording()
                                        if (file != null) {
                                            recordedFilePath = file.absolutePath
                                        }
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (recordedFilePath.isEmpty()) DangerRed else DarkCardBorder,
                                    contentColor = OffWhite
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("record_audio_button")
                            ) {
                                Icon(
                                    imageVector = if (recordedFilePath.isEmpty()) Icons.Default.Mic else Icons.Default.Refresh,
                                    contentDescription = "Record Voice",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            // Stop recording button
                            Button(
                                onClick = {
                                    val file = voiceRecorder.stopRecording()
                                    if (file != null && file.exists()) {
                                        recordedFilePath = file.absolutePath
                                        recordedDurationSeconds = recordingDuration
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DangerRed,
                                    contentColor = OffWhite
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("stop_recording_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Recording",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Playback button if recorded
                        if (recordedFilePath.isNotEmpty() && !isRecording) {
                            Button(
                                onClick = {
                                    if (isPlaying) {
                                        audioPlayer.pause()
                                    } else {
                                        audioPlayer.play(recordedFilePath)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintBlue,
                                    contentColor = PremiumBlack
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("playback_audio_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Recording",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    if (recordedFilePath.isNotEmpty() && !isRecording) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Audio recorded successfully",
                            fontSize = 12.sp,
                            fontFamily = SpaceGrotesk,
                            color = AnswerGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: SCHEDULE DATE & TIME
            Text(
                text = "2. Set Call Time & Date",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy - h:mm a", Locale.US)
                    val formatted = sdf.format(Date(scheduledTimestamp))

                    Text(
                        text = "Scheduled For:",
                        fontSize = 12.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatted,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BeigeYellow,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date & Time Picker buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = scheduledTimestamp }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        cal.set(Calendar.YEAR, year)
                                        cal.set(Calendar.MONTH, month)
                                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        scheduledTimestamp = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Date", fontFamily = SpaceGrotesk, color = OffWhite, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = scheduledTimestamp }
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        cal.set(Calendar.MINUTE, minute)
                                        scheduledTimestamp = cal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MintBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Time", fontFamily = SpaceGrotesk, color = OffWhite, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Chips
                    Text(
                        text = "Quick Presets:",
                        fontSize = 12.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PresetChip("In 1 Hour") {
                            val c = Calendar.getInstance()
                            c.add(Calendar.HOUR_OF_DAY, 1)
                            scheduledTimestamp = c.timeInMillis
                        }
                        PresetChip("Tomorrow") {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, 1)
                            c.set(Calendar.HOUR_OF_DAY, 20)
                            c.set(Calendar.MINUTE, 0)
                            scheduledTimestamp = c.timeInMillis
                        }
                        PresetChip("Next Week") {
                            val c = Calendar.getInstance()
                            c.add(Calendar.WEEK_OF_YEAR, 1)
                            c.set(Calendar.HOUR_OF_DAY, 20)
                            c.set(Calendar.MINUTE, 0)
                            scheduledTimestamp = c.timeInMillis
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PresetChip("In 1 Month") {
                            val c = Calendar.getInstance()
                            c.add(Calendar.MONTH, 1)
                            scheduledTimestamp = c.timeInMillis
                        }
                        PresetChip("In 1 Year") {
                            val c = Calendar.getInstance()
                            c.add(Calendar.YEAR, 1)
                            scheduledTimestamp = c.timeInMillis
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recurrence Options
                    Text(
                        text = "Call Repeat Frequency:",
                        fontSize = 12.sp,
                        fontFamily = SpaceGrotesk,
                        color = OffWhite.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val repeats = listOf(
                            Pair(EchoCall.REPEAT_ONCE, "Once"),
                            Pair(EchoCall.REPEAT_DAILY, "Everyday"),
                            Pair(EchoCall.REPEAT_WEEKLY, "Weekly"),
                            Pair(EchoCall.REPEAT_MONTHLY, "Monthly")
                        )
                        repeats.forEach { (type, label) ->
                            FilterChip(
                                selected = repeatType == type,
                                onClick = { repeatType = type },
                                label = { Text(label, fontFamily = SpaceGrotesk, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintBlue,
                                    selectedLabelColor = PremiumBlack,
                                    containerColor = DarkCardBorder,
                                    labelColor = OffWhite
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: CALL DETAILS
            Text(
                text = "3. Call Details",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Call Title *", color = OffWhite.copy(alpha = 0.8f)) },
                placeholder = { Text("e.g. Ask how I am doing, Remember dinner", color = OffWhite.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("call_title_input"),
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Note (Optional)", color = OffWhite.copy(alpha = 0.8f)) },
                placeholder = { Text("Context or notes for yourself when the call arrives", color = OffWhite.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("call_description_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintBlue,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: CALLER PROFILE OVERRIDE (OPTIONAL)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { useCustomCaller = !useCustomCaller }
            ) {
                Checkbox(
                    checked = useCustomCaller,
                    onCheckedChange = { useCustomCaller = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MintBlue,
                        uncheckedColor = DarkCardBorder,
                        checkmarkColor = PremiumBlack
                    )
                )
                Text(
                    text = "Customize caller for this Echo (Optional)",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = BeigeYellow
                )
            }

            AnimatedVisibility(visible = useCustomCaller) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CallerAvatar(
                            pfpUri = customCallerPfpUri,
                            presetId = selectedPresetId,
                            size = 72.dp,
                            borderColor = MintBlue
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (id in 1..4) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (customCallerPfpUri == null && selectedPresetId == id) 2.dp else 1.dp,
                                            color = if (customCallerPfpUri == null && selectedPresetId == id) BeigeYellow else DarkCardBorder,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            customCallerPfpUri = null
                                            selectedPresetId = id
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    CallerAvatar(pfpUri = null, presetId = id, size = 34.dp, borderColor = Color.Transparent)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                                    contentDescription = "Upload photo",
                                    tint = MintBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customCallerName,
                            onValueChange = { customCallerName = it },
                            label = { Text("Caller Name", color = OffWhite.copy(alpha = 0.8f)) },
                            placeholder = { Text(defaultProfile.callerName, color = OffWhite.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintBlue,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite,
                                focusedContainerColor = PremiumBlack,
                                unfocusedContainerColor = PremiumBlack
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 5: RINGTONE
            Text(
                text = "Ringtone",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MintBlue,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Pair("echo_pulse", "Echo Pulse"),
                    Pair("retro_phone", "Retro Phone"),
                    Pair("cosmic_call", "Cosmic Call")
                ).forEach { (id, name) ->
                    FilterChip(
                        selected = selectedRingtone == id,
                        onClick = { selectedRingtone = id },
                        label = { Text(name, fontFamily = SpaceGrotesk, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintBlue,
                            selectedLabelColor = PremiumBlack,
                            containerColor = DarkCard,
                            labelColor = OffWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SAVE BUTTON
            val canSave = title.isNotBlank() && recordedFilePath.isNotEmpty()

            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a title for the call", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (recordedFilePath.isEmpty()) {
                        Toast.makeText(context, "Please record your message first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onSave(
                        title.trim(),
                        description.trim(),
                        scheduledTimestamp,
                        repeatType,
                        repeatDayOfWeek,
                        recordedFilePath,
                        recordedDurationSeconds,
                        if (useCustomCaller && customCallerName.isNotBlank()) customCallerName else null,
                        if (useCustomCaller) customCallerPfpUri else null,
                        selectedRingtone,
                        null
                    )
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBlue,
                    contentColor = PremiumBlack,
                    disabledContainerColor = DarkCardBorder,
                    disabledContentColor = OffWhite.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_call_button")
            ) {
                Text(
                    text = if (initialCall != null) "Update Echo Call" else "Schedule Echo Call",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCardBorder)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = SpaceGrotesk,
            color = OffWhite
        )
    }
}
