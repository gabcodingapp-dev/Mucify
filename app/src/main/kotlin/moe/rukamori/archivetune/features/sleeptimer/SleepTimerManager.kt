/*
 * Mucify (2026)
 * Sleep Timer with Gradual Volume Fade
 * NEW FEATURE — not in original app
 * © Gab — github.com/gabcodingapp-dev
 */

package moe.rukamori.archivetune.features.sleeptimer

import android.content.Context
import android.media.AudioManager
import android.os.CountDownTimer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import moe.rukamori.archivetune.playback.MusicService
import javax.inject.Inject
import javax.inject.Singleton

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0L,
    val totalSeconds: Long = 0L,
    val fadeEnabled: Boolean = true,
    val currentVolumePercent: Int = 100
)

@Singleton
class SleepTimerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()
    
    private var timer: CountDownTimer? = null
    private var fadeTimer: CountDownTimer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalVolume: Int = 0
    
    fun startTimer(durationMinutes: Int, fadeOut: Boolean = true) {
        cancel()
        
        val totalMs = durationMinutes * 60 * 1000L
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        _state.value = SleepTimerState(
            isActive = true,
            remainingSeconds = durationMinutes * 60L,
            totalSeconds = durationMinutes * 60L,
            fadeEnabled = fadeOut,
            currentVolumePercent = 100
        )
        
        // Start fade 2 minutes before end (or at start if less than 2 min)
        val fadeStartMs = maxOf(0L, totalMs - 120_000L)
        val fadeDurationMs = totalMs - fadeStartMs
        
        if (fadeOut && fadeDurationMs > 0) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                startVolumeFade(fadeDurationMs)
            }, fadeStartMs)
        }
        
        timer = object : CountDownTimer(totalMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _state.value = _state.value.copy(
                    remainingSeconds = millisUntilFinished / 1000
                )
            }
            
            override fun onFinish() {
                _state.value = SleepTimerState()
                // Restore volume
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    originalVolume,
                    0
                )
            }
        }.start()
    }
    
    private fun startVolumeFade(durationMs: Long) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        fadeTimer = object : CountDownTimer(durationMs, 500L) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = millisUntilFinished.toFloat() / durationMs
                val newVolume = (startVolume * progress).toInt().coerceIn(0, maxVolume)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                
                _state.value = _state.value.copy(
                    currentVolumePercent = (progress * 100).toInt()
                )
            }
            
            override fun onFinish() {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            }
        }.start()
    }
    
    fun cancel() {
        timer?.cancel()
        fadeTimer?.cancel()
        if (originalVolume > 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
        }
        _state.value = SleepTimerState()
    }
    
    fun addMinutes(minutes: Int) {
        if (_state.value.isActive) {
            val remaining = _state.value.remainingSeconds
            cancel()
            startTimer(((remaining + minutes * 60) / 60).toInt(), fadeEnabled = true)
        }
    }
}
