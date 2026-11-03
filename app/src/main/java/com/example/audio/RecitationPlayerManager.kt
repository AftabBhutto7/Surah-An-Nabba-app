package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.Ayah
import com.example.data.model.Reciter
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentAyahNumber: Int = 1,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0,
    val surahCurrentPositionMs: Int = 0,
    val surahTotalDurationMs: Int = 287667,
    val reciter: Reciter = Reciter.ALAFASY,
    val repeatCountTotal: Int = 1, // 1 = play once, 3 = repeat 3x
    val repeatCountCurrent: Int = 1,
    val isRangeRepeatActive: Boolean = false,
    val rangeStartAyah: Int = 1,
    val rangeEndAyah: Int = 40,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesLeft: Int = 0,
    val errorMessage: String? = null
)

class RecitationPlayerManager(
    private val context: Context,
    private val quranRepository: QuranRepository
) {
    companion object {
        // Measured baseline durations (ms) for all 40 Ayahs of Surah An-Naba
        val DEFAULT_AYAH_DURATIONS = intArrayOf(
            6460, 5154, 6016, 4109, 6225, 4187, 3900, 4135, 4631, 4004,
            5075, 6381, 5441, 9359, 5310, 4213, 5702, 6956, 7191, 5833,
            6016, 3456, 5754, 7348, 5284, 5101, 6042, 5624, 5937, 7870,
            4501, 4422, 3221, 3743, 7296, 8523, 13513, 19730, 11345, 42659
        )
    }

    private val ayahDurations = IntArray(40) { index ->
        DEFAULT_AYAH_DURATIONS[index]
    }

    fun getTotalSurahDuration(): Int {
        return ayahDurations.sum()
    }

    fun getElapsedBeforeAyah(ayahNumber: Int): Int {
        var sum = 0
        val targetIndex = (ayahNumber - 1).coerceIn(0, 40)
        for (i in 0 until targetIndex) {
            sum += ayahDurations[i]
        }
        return sum
    }

    fun getSurahPosition(ayahNumber: Int, ayahPositionMs: Int): Int {
        val elapsedBefore = getElapsedBeforeAyah(ayahNumber)
        val curDuration = ayahDurations.getOrNull(ayahNumber - 1) ?: 5000
        val clampedAyahPos = ayahPositionMs.coerceIn(0, curDuration)
        return (elapsedBefore + clampedAyahPos).coerceIn(0, getTotalSurahDuration())
    }

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var sleepTimer: CountDownTimer? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val progressUpdater = object : Runnable {
        override fun run() {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    val ayahPos = mp.currentPosition
                    val curAyah = _playerState.value.currentAyahNumber
                    val surahPos = getSurahPosition(curAyah, ayahPos)
                    val totalSurah = getTotalSurahDuration()
                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = ayahPos,
                        totalDurationMs = mp.duration,
                        surahCurrentPositionMs = surahPos,
                        surahTotalDurationMs = totalSurah
                    )
                    handler.postDelayed(this, 300)
                }
            }
        }
    }

    fun playAyah(
        ayahNumber: Int,
        reciter: Reciter = _playerState.value.reciter,
        initialSeekMs: Int = 0
    ) {
        val maxAyah = quranRepository.getSurahData().totalAyahs
        val clampedAyah = ayahNumber.coerceIn(1, maxAyah)
        val ayah = quranRepository.getAyah(clampedAyah) ?: return

        val audioUrl = when (reciter) {
            Reciter.ALAFASY -> ayah.audioUrlAlafasy
            Reciter.ABDUL_BASIT -> ayah.audioUrlAbdulBasit
            Reciter.GHAMADI -> ayah.audioUrlGhamadi
        }

        stopPlayback()

        val totalSurah = getTotalSurahDuration()
        val surahPos = getSurahPosition(clampedAyah, initialSeekMs)

        _playerState.value = _playerState.value.copy(
            isLoading = true,
            currentAyahNumber = clampedAyah,
            reciter = reciter,
            currentPositionMs = initialSeekMs,
            surahCurrentPositionMs = surahPos,
            surahTotalDurationMs = totalSurah,
            errorMessage = null
        )

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { preparedMp ->
                    if (preparedMp.duration > 0 && clampedAyah in 1..40) {
                        ayahDurations[clampedAyah - 1] = preparedMp.duration
                    }
                    val updatedTotalSurah = getTotalSurahDuration()
                    if (initialSeekMs > 0) {
                        try {
                            preparedMp.seekTo(initialSeekMs)
                        } catch (e: Exception) {
                            Log.w("AudioPlayer", "Seek error: ${e.message}")
                        }
                    }
                    val updatedSurahPos = getSurahPosition(clampedAyah, if (initialSeekMs > 0) initialSeekMs else 0)

                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        isPlaying = true,
                        totalDurationMs = preparedMp.duration,
                        currentPositionMs = if (initialSeekMs > 0) initialSeekMs else 0,
                        surahCurrentPositionMs = updatedSurahPos,
                        surahTotalDurationMs = updatedTotalSurah
                    )
                    // Set speed if supported
                    try {
                        preparedMp.playbackParams = preparedMp.playbackParams.setSpeed(_playerState.value.playbackSpeed)
                    } catch (e: Exception) {
                        Log.w("AudioPlayer", "Playback speed not supported: ${e.message}")
                    }
                    preparedMp.start()
                    handler.post(progressUpdater)
                }
                setOnCompletionListener {
                    handleAyahCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        isPlaying = false,
                        errorMessage = "Audio playback failed (code: $what, extra: $extra). Check internet connection."
                    )
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                isLoading = false,
                isPlaying = false,
                errorMessage = "Failed to load audio: ${e.localizedMessage}"
            )
        }
    }

    private fun handleAyahCompletion() {
        handler.removeCallbacks(progressUpdater)
        val state = _playerState.value

        // Check single ayah repetition
        if (state.repeatCountCurrent < state.repeatCountTotal) {
            _playerState.value = state.copy(repeatCountCurrent = state.repeatCountCurrent + 1)
            playAyah(state.currentAyahNumber)
            return
        }

        // Reset current repeat count
        _playerState.value = state.copy(repeatCountCurrent = 1)

        // Range repeat or normal advance
        if (state.isRangeRepeatActive) {
            if (state.currentAyahNumber < state.rangeEndAyah) {
                playAyah(state.currentAyahNumber + 1)
            } else {
                // Loop back to start of range
                playAyah(state.rangeStartAyah)
            }
        } else {
            // Normal sequential reading: advance if not at end
            val maxAyah = quranRepository.getSurahData().totalAyahs
            if (state.currentAyahNumber < maxAyah) {
                playAyah(state.currentAyahNumber + 1)
            } else {
                // Completed the full Surah! Slider is 100% full.
                val totalSurah = getTotalSurahDuration()
                _playerState.value = _playerState.value.copy(
                    isPlaying = false,
                    currentPositionMs = 0,
                    surahCurrentPositionMs = totalSurah
                )
            }
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        if (mp != null) {
            if (mp.isPlaying) {
                mp.pause()
                handler.removeCallbacks(progressUpdater)
                _playerState.value = _playerState.value.copy(isPlaying = false)
            } else {
                mp.start()
                handler.post(progressUpdater)
                _playerState.value = _playerState.value.copy(isPlaying = true)
            }
        } else {
            playAyah(_playerState.value.currentAyahNumber)
        }
    }

    fun seekToSurahPosition(targetSurahMs: Int) {
        val total = getTotalSurahDuration()
        val clampedTarget = targetSurahMs.coerceIn(0, total)

        var accumulated = 0
        var targetAyah = 1
        var offsetInAyah = 0
        for (i in 0 until 40) {
            val dur = ayahDurations[i]
            if (accumulated + dur > clampedTarget || i == 39) {
                targetAyah = i + 1
                offsetInAyah = (clampedTarget - accumulated).coerceIn(0, dur)
                break
            }
            accumulated += dur
        }

        if (_playerState.value.currentAyahNumber == targetAyah && mediaPlayer != null) {
            mediaPlayer?.seekTo(offsetInAyah)
            _playerState.value = _playerState.value.copy(
                currentPositionMs = offsetInAyah,
                surahCurrentPositionMs = clampedTarget
            )
        } else {
            playAyah(targetAyah, initialSeekMs = offsetInAyah)
        }
    }

    fun seekTo(positionMs: Int) {
        seekToSurahPosition(positionMs)
    }

    fun nextAyah() {
        val maxAyah = quranRepository.getSurahData().totalAyahs
        val next = (_playerState.value.currentAyahNumber + 1).coerceAtMost(maxAyah)
        playAyah(next)
    }

    fun previousAyah() {
        val prev = (_playerState.value.currentAyahNumber - 1).coerceAtLeast(1)
        playAyah(prev)
    }

    fun setReciter(reciter: Reciter) {
        val wasPlaying = _playerState.value.isPlaying
        _playerState.value = _playerState.value.copy(reciter = reciter)
        if (wasPlaying) {
            playAyah(_playerState.value.currentAyahNumber, reciter)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        mediaPlayer?.let { mp ->
            try {
                mp.playbackParams = mp.playbackParams.setSpeed(speed)
            } catch (e: Exception) {
                Log.w("AudioPlayer", "Speed adjust error: ${e.message}")
            }
        }
    }

    fun setRepeatCount(count: Int) {
        _playerState.value = _playerState.value.copy(
            repeatCountTotal = count,
            repeatCountCurrent = 1
        )
    }

    fun setRangeRepeat(enabled: Boolean, start: Int = 1, end: Int = 40) {
        val maxAyah = quranRepository.getSurahData().totalAyahs
        _playerState.value = _playerState.value.copy(
            isRangeRepeatActive = enabled,
            rangeStartAyah = start.coerceIn(1, maxAyah),
            rangeEndAyah = end.coerceIn(start, maxAyah)
        )
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimer?.cancel()
        if (minutes <= 0) {
            _playerState.value = _playerState.value.copy(sleepTimerMinutesLeft = 0)
            return
        }

        _playerState.value = _playerState.value.copy(sleepTimerMinutesLeft = minutes)
        sleepTimer = object : CountDownTimer((minutes * 60 * 1000).toLong(), 60000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minsLeft = (millisUntilFinished / 60000).toInt() + 1
                _playerState.value = _playerState.value.copy(sleepTimerMinutesLeft = minsLeft)
            }

            override fun onFinish() {
                _playerState.value = _playerState.value.copy(sleepTimerMinutesLeft = 0)
                stopPlayback()
            }
        }.start()
    }

    fun stopPlayback() {
        handler.removeCallbacks(progressUpdater)
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                reset()
                release()
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error stopping player", e)
            }
        }
        mediaPlayer = null
        _playerState.value = _playerState.value.copy(isPlaying = false, isLoading = false)
    }

    fun release() {
        sleepTimer?.cancel()
        stopPlayback()
    }
}
