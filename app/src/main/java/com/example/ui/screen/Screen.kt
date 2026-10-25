package com.example.ui.screen

import com.example.data.model.EchoCall

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Main : Screen()
    data class CreateEditCall(val callId: Long? = null) : Screen()
    object EditProfile : Screen()
    object Credits : Screen()
    data class IncomingCall(val call: EchoCall) : Screen()
    data class ActiveCall(val call: EchoCall) : Screen()
}
