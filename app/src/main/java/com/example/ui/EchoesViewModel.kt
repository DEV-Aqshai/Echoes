package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.EchoCall
import com.example.data.repository.EchoRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.UserProfile
import com.example.ui.screen.Screen
import com.example.util.AudioPlayer
import com.example.util.RingtonePlayer
import com.example.util.VoiceRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EchoesViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val repository = EchoRepository(context)
    private val preferencesRepository = UserPreferencesRepository(context)

    val userProfile: StateFlow<UserProfile> = preferencesRepository.userProfile

    val allCalls: StateFlow<List<EchoCall>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Splash).apply {
        viewModelScope.launch {
            _screenStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.Main
            }
        }
    }

    val voiceRecorder = VoiceRecorder(context)
    val audioPlayer = AudioPlayer(context)
    val ringtonePlayer = RingtonePlayer(context)

    // Current call being created or edited
    private val _editingCall = MutableStateFlow<EchoCall?>(null)
    val editingCall: StateFlow<EchoCall?> = _editingCall.asStateFlow()

    init {
        cleanupDemoEchoes()
    }

    private fun cleanupDemoEchoes() {
        viewModelScope.launch {
            try {
                // Ensure no leftover demo echoes appear
                val call1 = repository.getCallById(1L)
                if (call1 != null && (call1.title == "Ask how I am doing" || call1.title == "Remember to buy dinner")) {
                    repository.deleteCall(call1)
                }
                val call2 = repository.getCallById(2L)
                if (call2 != null && (call2.title == "Remember to buy dinner" || call2.title == "Ask how I am doing")) {
                    repository.deleteCall(call2)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value.toMutableList()
        current.add(screen)
        _screenStack.value = current
    }

    fun replaceScreen(screen: Screen) {
        _screenStack.value = listOf(screen)
    }

    fun navigateBack(): Boolean {
        audioPlayer.stop()
        ringtonePlayer.stopRinging()
        voiceRecorder.cancelRecording()

        val current = _screenStack.value.toMutableList()
        return if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
            true
        } else {
            false
        }
    }

    fun completeOnboarding(
        callerName: String,
        callerPfpUri: String?,
        presetAvatarId: Int,
        callBgHex: String,
        ringtoneId: String = "echo_pulse"
    ) {
        preferencesRepository.setOnboardingCompleted(
            callerName = callerName,
            callerPfpUri = callerPfpUri,
            presetAvatarId = presetAvatarId,
            callBgHex = callBgHex,
            ringtoneId = ringtoneId
        )
        replaceScreen(Screen.Main)
    }

    fun updateProfile(
        callerName: String,
        callerPfpUri: String?,
        presetAvatarId: Int,
        callBgHex: String,
        ringtoneId: String
    ) {
        preferencesRepository.updateProfile(
            callerName = callerName,
            callerPfpUri = callerPfpUri,
            presetAvatarId = presetAvatarId,
            callBgHex = callBgHex,
            ringtoneId = ringtoneId
        )
        navigateBack()
    }

    fun startCreateCall() {
        _editingCall.value = null
        navigateTo(Screen.CreateEditCall(null))
    }

    fun startEditCall(call: EchoCall) {
        _editingCall.value = call
        navigateTo(Screen.CreateEditCall(call.id))
    }

    fun saveCall(
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
    ) {
        viewModelScope.launch {
            val existing = _editingCall.value
            if (existing != null) {
                val updated = existing.copy(
                    title = title,
                    description = description,
                    scheduledTimestamp = scheduledTimestamp,
                    repeatType = repeatType,
                    repeatDayOfWeek = repeatDayOfWeek,
                    audioFilePath = audioFilePath,
                    audioDurationSeconds = audioDurationSeconds,
                    customCallerName = customCallerName,
                    customCallerPfpUri = customCallerPfpUri,
                    ringtoneId = ringtoneId,
                    callBgHex = callBgHex,
                    isTriggered = false
                )
                repository.updateCall(updated)
            } else {
                val newCall = EchoCall(
                    title = title,
                    description = description,
                    scheduledTimestamp = scheduledTimestamp,
                    repeatType = repeatType,
                    repeatDayOfWeek = repeatDayOfWeek,
                    audioFilePath = audioFilePath,
                    audioDurationSeconds = audioDurationSeconds,
                    customCallerName = customCallerName,
                    customCallerPfpUri = customCallerPfpUri,
                    ringtoneId = ringtoneId,
                    callBgHex = callBgHex
                )
                repository.saveCall(newCall)
            }
            navigateBack()
        }
    }

    fun deleteCall(call: EchoCall) {
        viewModelScope.launch {
            repository.deleteCall(call)
        }
    }

    fun simulateCall(call: EchoCall) {
        ringtonePlayer.startRinging(call.ringtoneId.ifEmpty { userProfile.value.ringtoneId })
        navigateTo(Screen.IncomingCall(call))
    }

    fun answerCall(call: EchoCall) {
        ringtonePlayer.stopRinging()
        replaceScreen(Screen.ActiveCall(call))
        audioPlayer.play(call.audioFilePath) {
            // playback completed
        }
    }

    fun declineCall() {
        ringtonePlayer.stopRinging()
        replaceScreen(Screen.Main)
    }

    fun endActiveCall() {
        audioPlayer.stop()
        replaceScreen(Screen.Main)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        ringtonePlayer.stopRinging()
        voiceRecorder.cancelRecording()
    }
}
