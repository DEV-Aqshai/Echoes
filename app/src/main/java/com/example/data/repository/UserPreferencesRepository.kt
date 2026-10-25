package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val callerName: String = "Future Me",
    val callerPfpUri: String? = null,
    val presetAvatarId: Int = 1,
    val callBgHex: String = "#1F1C1C",
    val ringtoneId: String = "echo_pulse",
    val hasCompletedOnboarding: Boolean = false
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("echoes_user_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        return UserProfile(
            callerName = prefs.getString("caller_name", "Future Me") ?: "Future Me",
            callerPfpUri = prefs.getString("caller_pfp_uri", null),
            presetAvatarId = prefs.getInt("preset_avatar_id", 1),
            callBgHex = prefs.getString("call_bg_hex", "#1F1C1C") ?: "#1F1C1C",
            ringtoneId = prefs.getString("ringtone_id", "echo_pulse") ?: "echo_pulse",
            hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
        )
    }

    fun updateProfile(
        callerName: String,
        callerPfpUri: String?,
        presetAvatarId: Int,
        callBgHex: String,
        ringtoneId: String
    ) {
        prefs.edit()
            .putString("caller_name", callerName)
            .putString("caller_pfp_uri", callerPfpUri)
            .putInt("preset_avatar_id", presetAvatarId)
            .putString("call_bg_hex", callBgHex)
            .putString("ringtone_id", ringtoneId)
            .apply()
        _userProfile.value = loadProfile()
    }

    fun setOnboardingCompleted(
        callerName: String,
        callerPfpUri: String?,
        presetAvatarId: Int,
        callBgHex: String,
        ringtoneId: String = "echo_pulse"
    ) {
        prefs.edit()
            .putString("caller_name", callerName)
            .putString("caller_pfp_uri", callerPfpUri)
            .putInt("preset_avatar_id", presetAvatarId)
            .putString("call_bg_hex", callBgHex)
            .putString("ringtone_id", ringtoneId)
            .putBoolean("has_completed_onboarding", true)
            .apply()
        _userProfile.value = loadProfile()
    }
}
