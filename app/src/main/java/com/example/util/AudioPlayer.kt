package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    fun play(filePath: String, onCompletion: (() -> Unit)? = null) {
        stop()
        if (filePath.isBlank()) {
            Log.e("AudioPlayer", "Audio file path is blank")
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            Log.e("AudioPlayer", "File does not exist: $filePath")
            return
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = _isSpeakerOn.value

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.fromFile(file))
                setVolume(1.0f, 1.0f)

                setOnPreparedListener { mp ->
                    try {
                        _totalDuration.value = mp.duration
                        mp.start()
                        _isPlaying.value = true
                        startProgressTracking()
                        Log.d("AudioPlayer", "Audio playback started successfully, duration: ${mp.duration}ms")
                    } catch (e: Exception) {
                        Log.e("AudioPlayer", "Error starting playback after prepare", e)
                    }
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = _totalDuration.value
                    progressJob?.cancel()
                    onCompletion?.invoke()
                    Log.d("AudioPlayer", "Audio playback completed")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what extra=$extra")
                    _isPlaying.value = false
                    progressJob?.cancel()
                    false
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error setting up MediaPlayer for $filePath", e)
            stop()
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                try {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            _currentPosition.value = it.currentPosition
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient exceptions
                }
                delay(80)
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error pausing playback", e)
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                it.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error resuming playback", e)
        }
    }

    fun replay(filePath: String, onCompletion: (() -> Unit)? = null) {
        play(filePath, onCompletion)
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _currentPosition.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error seeking playback", e)
        }
    }

    fun toggleSpeaker() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val newState = !_isSpeakerOn.value
        _isSpeakerOn.value = newState
        try {
            audioManager.isSpeakerphoneOn = newState
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error toggling speaker", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        _isPlaying.value = false
        _currentPosition.value = 0
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping player", e)
        } finally {
            mediaPlayer = null
        }
    }
}
