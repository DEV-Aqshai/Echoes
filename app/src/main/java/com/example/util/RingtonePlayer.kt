package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R

class RingtonePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun startRinging(ringtoneId: String = "echo_pulse") {
        stopRinging()
        try {
            val resId = when (ringtoneId) {
                "retro_phone" -> R.raw.retro_phone
                "cosmic_call" -> R.raw.cosmic_call
                else -> R.raw.echo_pulse
            }

            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error starting ringtone audio", e)
            try {
                val defaultUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer.create(context, defaultUri)?.apply {
                    isLooping = true
                    start()
                }
            } catch (err: Exception) {
                Log.e("RingtonePlayer", "Fallback ringtone failed", err)
            }
        }

        // Start vibration
        try {
            val pattern = longArrayOf(0, 800, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error vibrating", e)
        }
    }

    fun previewRingtone(ringtoneId: String) {
        stopRinging()
        try {
            val resId = when (ringtoneId) {
                "retro_phone" -> R.raw.retro_phone
                "cosmic_call" -> R.raw.cosmic_call
                else -> R.raw.echo_pulse
            }
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                isLooping = false
                start()
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Preview ringtone error", e)
        }
    }

    fun stopRinging() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error stopping ringtone", e)
        } finally {
            mediaPlayer = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error cancelling vibration", e)
        }
    }
}
