package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
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
import java.io.FileInputStream

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

        // Resolve audio file using EchoStorage helper (supports echoes/records/10029.mp3)
        val file: File? = EchoStorage.resolveAudioFile(context, filePath)

        if (file == null || !file.exists() || file.length() == 0L) {
            Log.e("AudioPlayer", "No valid recorded audio file found at '$filePath' or in echoes/records/10029.mp3")
            return
        }

        Log.d("AudioPlayer", "Playing audio from: ${file.absolutePath} (size: ${file.length()} bytes)")

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let { am ->
                am.mode = AudioManager.MODE_NORMAL
                am.isSpeakerphoneOn = _isSpeakerOn.value
                val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVol < maxVol * 0.7f) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.85f).toInt(), 0)
                }
            }

            val player = MediaPlayer()
            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            // Open using FileDescriptor for 100% Android compatibility
            FileInputStream(file).use { fis ->
                player.setDataSource(fis.fd)
            }

            player.setVolume(1.0f, 1.0f)

            player.setOnPreparedListener { mp ->
                try {
                    _totalDuration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracking()
                    Log.d("AudioPlayer", "Playback started successfully. Duration: ${mp.duration}ms")
                } catch (e: Exception) {
                    Log.e("AudioPlayer", "Error starting playback in onPrepared", e)
                }
            }

            player.setOnCompletionListener {
                _isPlaying.value = false
                _currentPosition.value = _totalDuration.value
                progressJob?.cancel()
                onCompletion?.invoke()
                Log.d("AudioPlayer", "Playback finished")
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayer", "MediaPlayer error: what=$what extra=$extra")
                _isPlaying.value = false
                progressJob?.cancel()
                false
            }

            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to initialize MediaPlayer for ${file.absolutePath}", e)
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
