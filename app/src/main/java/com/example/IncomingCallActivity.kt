package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.example.data.db.EchoDatabase
import com.example.data.model.EchoCall
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.screen.ActiveCallScreen
import com.example.ui.screen.IncomingCallScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PremiumBlack
import com.example.util.AudioPlayer
import com.example.util.EchoStorage
import com.example.util.RingtonePlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IncomingCallActivity : ComponentActivity() {

    private lateinit var ringtonePlayer: RingtonePlayer
    private lateinit var audioPlayer: AudioPlayer
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn on screen and show over lockscreen even when phone is locked or app is closed
        configureLockScreenFlags()

        ringtonePlayer = RingtonePlayer(this)
        audioPlayer = AudioPlayer(this)

        val callId = intent.getLongExtra(EXTRA_CALL_ID, -1L)
        val initialTitle = intent.getStringExtra(EXTRA_CALL_TITLE) ?: "Echo from the Future"

        setContent {
            MyApplicationTheme {
                IncomingCallScreenContent(
                    callId = callId,
                    fallbackTitle = initialTitle,
                    ringtonePlayer = ringtonePlayer,
                    audioPlayer = audioPlayer,
                    onFinish = {
                        dismissNotification(callId)
                        finish()
                    }
                )
            }
        }
    }

    private fun configureLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Echoes:IncomingCallScreenLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L /* 10 minutes max */)
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error acquiring wake lock", e)
        }
    }

    private fun dismissNotification(callId: Long) {
        try {
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.cancel(1000 + callId.toInt())
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtonePlayer.stopRinging()
        audioPlayer.stop()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        const val ACTION_INCOMING_CALL = "com.example.ACTION_INCOMING_CALL"
        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_CALL_TITLE = "extra_call_title"
    }
}

@Composable
private fun IncomingCallScreenContent(
    callId: Long,
    fallbackTitle: String,
    ringtonePlayer: RingtonePlayer,
    audioPlayer: AudioPlayer,
    onFinish: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val defaultProfile by prefsRepo.userProfile.collectAsState()

    var echoCall by remember { mutableStateOf<EchoCall?>(null) }
    var isCallAnswered by remember { mutableStateOf(false) }

    LaunchedEffect(callId) {
        withContext(Dispatchers.IO) {
            val db = EchoDatabase.getDatabase(context)
            val call = if (callId > 0) db.echoCallDao().getCallById(callId) else null
            val resolvedFile = EchoStorage.resolveAudioFile(context, call?.audioFilePath)
            val path = resolvedFile?.absolutePath ?: (call?.audioFilePath ?: "")

            echoCall = if (call != null) {
                call.copy(audioFilePath = path)
            } else {
                EchoCall(
                    id = callId,
                    title = fallbackTitle,
                    description = "Incoming call scheduled by you across time.",
                    scheduledTimestamp = System.currentTimeMillis(),
                    audioFilePath = path,
                    ringtoneId = defaultProfile.ringtoneId
                )
            }
        }
    }

    // Start ringtone immediately when call details are ready
    LaunchedEffect(echoCall) {
        val call = echoCall
        if (call != null && !isCallAnswered) {
            val ringId = if (call.ringtoneId.isNotEmpty()) call.ringtoneId else defaultProfile.ringtoneId
            ringtonePlayer.startRinging(ringId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ringtonePlayer.stopRinging()
            audioPlayer.stop()
        }
    }

    BackHandler {
        if (isCallAnswered) {
            audioPlayer.stop()
            onFinish()
        } else {
            ringtonePlayer.stopRinging()
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
            .safeDrawingPadding()
    ) {
        val currentCall = echoCall
        if (currentCall != null) {
            if (!isCallAnswered) {
                IncomingCallScreen(
                    call = currentCall,
                    defaultProfile = defaultProfile,
                    onAnswer = {
                        ringtonePlayer.stopRinging()
                        isCallAnswered = true
                    },
                    onDecline = {
                        ringtonePlayer.stopRinging()
                        onFinish()
                    }
                )
            } else {
                ActiveCallScreen(
                    call = currentCall,
                    defaultProfile = defaultProfile,
                    audioPlayer = audioPlayer,
                    onEndCall = {
                        audioPlayer.stop()
                        onFinish()
                    }
                )
            }
        }
    }
}
