package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
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
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    fun startRecording(): File? {
        stopRecording()
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "echo_${System.currentTimeMillis()}.m4a")
        currentOutputFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                _isRecording.value = true
                _recordingDurationSeconds.value = 0
                startAmplitudeTracking()
            } catch (e: IOException) {
                Log.e("VoiceRecorder", "Error starting recording", e)
                release()
                return null
            } catch (e: IllegalStateException) {
                Log.e("VoiceRecorder", "Illegal state starting recording", e)
                release()
                return null
            }
        }
        return file
    }

    private fun startAmplitudeTracking() {
        amplitudeJob?.cancel()
        val startTime = System.currentTimeMillis()
        amplitudeJob = scope.launch {
            while (isActive && _isRecording.value) {
                val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _recordingDurationSeconds.value = elapsed
                val maxAmp = try {
                    recorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }
                // Normalize 0..32767 to 0f..1f
                _currentAmplitude.value = (maxAmp / 32767f).coerceIn(0f, 1f)
                delay(100)
            }
        }
    }

    fun stopRecording(): File? {
        amplitudeJob?.cancel()
        _isRecording.value = false
        _currentAmplitude.value = 0f
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error stopping recorder", e)
        } finally {
            recorder = null
        }
        return currentOutputFile
    }

    fun cancelRecording() {
        stopRecording()
        currentOutputFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }
        currentOutputFile = null
        _recordingDurationSeconds.value = 0
    }
}
