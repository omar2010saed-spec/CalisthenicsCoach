package com.calisthenics.coach.logic

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Optional offline sound (system tones) and vibration. Nothing here needs permissions beyond VIBRATE. */
class Feedback(context: Context) {
    private val appContext = context.applicationContext
    private var tone: ToneGenerator? = null

    private fun toneGen(): ToneGenerator? {
        if (tone == null) {
            tone = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (e: Exception) {
                null
            }
        }
        return tone
    }

    @Suppress("DEPRECATION")
    private fun vibrator(): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    fun tick(sound: Boolean, vibrate: Boolean) {
        if (sound) toneGen()?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        if (vibrate) buzz(30)
    }

    fun done(sound: Boolean, vibrate: Boolean) {
        if (sound) toneGen()?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        if (vibrate) buzz(200)
    }

    @Suppress("DEPRECATION")
    private fun buzz(ms: Long) {
        val v = vibrator() ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(ms)
            }
        } catch (e: Exception) {
            // vibration is optional
        }
    }

    fun release() {
        try {
            tone?.release()
        } catch (e: Exception) {
            // ignore
        }
        tone = null
    }
}
