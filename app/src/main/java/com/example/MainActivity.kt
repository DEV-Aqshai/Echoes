package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.EchoDatabase
import com.example.service.CallAlarmReceiver
import com.example.ui.EchoesViewModel
import com.example.ui.screen.ActiveCallScreen
import com.example.ui.screen.CreateEditCallScreen
import com.example.ui.screen.CreditsScreen
import com.example.ui.screen.EditProfileScreen
import com.example.ui.screen.IncomingCallScreen
import com.example.ui.screen.MainScreen
import com.example.ui.screen.OnboardingScreen
import com.example.ui.screen.Screen
import com.example.ui.screen.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PremiumBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var viewModelInstance: EchoesViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: EchoesViewModel = viewModel()
                viewModelInstance = viewModel

                EchoesApp(
                    viewModel = viewModel,
                    onExitApp = { finish() }
                )
            }
        }

        handleIncomingCallIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingCallIntent(intent)
    }

    private fun handleIncomingCallIntent(intent: Intent?) {
        if (intent == null) return
        val callId = intent.getLongExtra(CallAlarmReceiver.EXTRA_CALL_ID, -1L)
        if (callId > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = EchoDatabase.getDatabase(applicationContext)
                val call = db.echoCallDao().getCallById(callId)
                if (call != null) {
                    withContext(Dispatchers.Main) {
                        viewModelInstance?.simulateCall(call)
                    }
                }
            }
        }
    }
}

@Composable
fun EchoesApp(
    viewModel: EchoesViewModel,
    onExitApp: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allCalls by viewModel.allCalls.collectAsState()
    val editingCall by viewModel.editingCall.collectAsState()

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val context = viewModel.getApplication<EchoesApplication>().applicationContext
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Back handling
    BackHandler {
        when (currentScreen) {
            is Screen.Splash -> onExitApp()
            is Screen.Main -> onExitApp()
            is Screen.IncomingCall -> {
                viewModel.declineCall()
            }
            is Screen.ActiveCall -> {
                viewModel.endActiveCall()
            }
            else -> {
                if (!viewModel.navigateBack()) {
                    onExitApp()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
            .safeDrawingPadding()
    ) {
        when (val screen = currentScreen) {
            is Screen.Splash -> {
                SplashScreen(
                    hasCompletedOnboarding = userProfile.hasCompletedOnboarding,
                    onNavigateNext = { completed ->
                        if (completed) {
                            viewModel.replaceScreen(Screen.Main)
                        } else {
                            viewModel.replaceScreen(Screen.Onboarding)
                        }
                    }
                )
            }

            is Screen.Onboarding -> {
                OnboardingScreen(
                    onComplete = { callerName, pfpUri, presetId, callBgHex ->
                        viewModel.completeOnboarding(
                            callerName = callerName,
                            callerPfpUri = pfpUri,
                            presetAvatarId = presetId,
                            callBgHex = callBgHex
                        )
                    }
                )
            }

            is Screen.Main -> {
                MainScreen(
                    calls = allCalls,
                    onNewCallClick = { viewModel.startCreateCall() },
                    onSettingsClick = { viewModel.navigateTo(Screen.EditProfile) },
                    onCreditsClick = { viewModel.navigateTo(Screen.Credits) },
                    onEditCallClick = { call -> viewModel.startEditCall(call) },
                    onDeleteCallClick = { call -> viewModel.deleteCall(call) },
                    onSimulateCallClick = { call -> viewModel.simulateCall(call) }
                )
            }

            is Screen.CreateEditCall -> {
                CreateEditCallScreen(
                    initialCall = editingCall,
                    defaultProfile = userProfile,
                    voiceRecorder = viewModel.voiceRecorder,
                    audioPlayer = viewModel.audioPlayer,
                    onBack = { viewModel.navigateBack() },
                    onSave = { title, desc, scheduled, repeatType, repeatDay, audioPath, audioSec, customName, customPfp, ringtone, bgHex ->
                        viewModel.saveCall(
                            title = title,
                            description = desc,
                            scheduledTimestamp = scheduled,
                            repeatType = repeatType,
                            repeatDayOfWeek = repeatDay,
                            audioFilePath = audioPath,
                            audioDurationSeconds = audioSec,
                            customCallerName = customName,
                            customCallerPfpUri = customPfp,
                            ringtoneId = ringtone,
                            callBgHex = bgHex
                        )
                    }
                )
            }

            is Screen.EditProfile -> {
                EditProfileScreen(
                    currentProfile = userProfile,
                    ringtonePlayer = viewModel.ringtonePlayer,
                    onBack = { viewModel.navigateBack() },
                    onSave = { name, pfp, preset, bgHex, ringtone ->
                        viewModel.updateProfile(
                            callerName = name,
                            callerPfpUri = pfp,
                            presetAvatarId = preset,
                            callBgHex = bgHex,
                            ringtoneId = ringtone
                        )
                    }
                )
            }

            is Screen.Credits -> {
                CreditsScreen(
                    onBack = { viewModel.navigateBack() }
                )
            }

            is Screen.IncomingCall -> {
                IncomingCallScreen(
                    call = screen.call,
                    defaultProfile = userProfile,
                    onAnswer = { viewModel.answerCall(screen.call) },
                    onDecline = { viewModel.declineCall() }
                )
            }

            is Screen.ActiveCall -> {
                ActiveCallScreen(
                    call = screen.call,
                    defaultProfile = userProfile,
                    audioPlayer = viewModel.audioPlayer,
                    onEndCall = { viewModel.endActiveCall() }
                )
            }
        }
    }
}
