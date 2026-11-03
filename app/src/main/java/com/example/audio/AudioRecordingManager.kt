package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class RecordingState(
    val isRecording: Boolean = false,
    val isPlayingRecording: Boolean = false,
    val hasRecording: Boolean = false,
    val recordingDurationSec: Int = 0,
    val currentAyah: Int = 1,
    val errorMessage: String? = null
)

class AudioRecordingManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordFile: File? = null

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private fun getFileForAyah(ayahNumber: Int): File {
        val dir = File(context.cacheDir, "recitations")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "ayah_${ayahNumber}_recitation.m4a")
    }

    fun startRecording(ayahNumber: Int): Boolean {
        stopPlayback()
        recordFile = getFileForAyah(ayahNumber)

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(recordFile!!.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _recordingState.value = _recordingState.value.copy(
                isRecording = true,
                currentAyah = ayahNumber,
                hasRecording = false,
                errorMessage = null
            )
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Recording start failed", e)
            _recordingState.value = _recordingState.value.copy(
                isRecording = false,
                errorMessage = "Microphone error: ${e.localizedMessage}"
            )
            false
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Stop recording failed", e)
        }
        mediaRecorder = null
        val exists = recordFile?.exists() == true
        _recordingState.value = _recordingState.value.copy(
            isRecording = false,
            hasRecording = exists
        )
    }

    fun playUserRecording(ayahNumber: Int) {
        val file = getFileForAyah(ayahNumber)
        if (!file.exists()) {
            _recordingState.value = _recordingState.value.copy(errorMessage = "No recording found for Ayah $ayahNumber")
            return
        }

        stopPlayback()

        try {
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    it.start()
                    _recordingState.value = _recordingState.value.copy(isPlayingRecording = true)
                }
                setOnCompletionListener {
                    _recordingState.value = _recordingState.value.copy(isPlayingRecording = false)
                }
                prepareAsync()
            }
            player = mp
        } catch (e: Exception) {
            _recordingState.value = _recordingState.value.copy(
                isPlayingRecording = false,
                errorMessage = "Failed to play recording: ${e.localizedMessage}"
            )
        }
    }

    fun stopPlayback() {
        player?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        player = null
        _recordingState.value = _recordingState.value.copy(isPlayingRecording = false)
    }

    fun deleteRecording(ayahNumber: Int) {
        stopPlayback()
        val file = getFileForAyah(ayahNumber)
        if (file.exists()) {
            file.delete()
        }
        _recordingState.value = _recordingState.value.copy(
            hasRecording = false,
            isPlayingRecording = false
        )
    }

    fun hasRecordingForAyah(ayahNumber: Int): Boolean {
        return getFileForAyah(ayahNumber).exists()
    }
}
