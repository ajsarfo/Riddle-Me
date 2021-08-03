package com.sarftec.riddleme.tools

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.sarftec.riddleme.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {

    enum class Sound(val resId: Int = -10, val volume: Float = 1f, val rate: Float = 1f) {
        ANSWER_FOUND(R.raw.collect, 0.5f),
        CANCEL(R.raw.go_back, volume = 0.3f, rate = 1.2f),
        TAP(R.raw.tap, 1f),
        RECEIVE_COINS(R.raw.points, 0.5f),
        CONGRATULATIONS(R.raw.congratulation, 0.4f),
        GAME_FINISHED(),
        DIALOG_REVEAL(R.raw.pop_up, 0.5f)
    }

    private val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val builder = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(builder)
            .build()
    } else {
        SoundPool(2, AudioManager.STREAM_MUSIC, 0)
    }

    private val loadedSounds = EnumMap<Sound, Int>(Sound::class.java)

    init {
        Sound.values()
            .filter { it.resId != -10 }
            .forEach {sound ->
                loadedSounds[sound] = soundPool.load(context, sound.resId, 1)
            }
    }

    /*
    private val mediaPlayer = MediaPlayer().apply {
        setOnPreparedListener {
            start()
        }
        setOnCompletionListener {
            reset()
        }
    }
     */

    fun playSound(sound: Sound) {
        loadedSounds[sound]?.let {
            soundPool.play(it, sound.volume, sound.volume, sound.ordinal, 0, sound.rate)
        }
       /*
        if (sound.resId == -10) return
        val assetFileDescriptor = context.resources.openRawResourceFd(sound.resId)
        mediaPlayer.run {
            reset()
            setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
            prepareAsync()
        }
        */
    }

    fun destroy() {
        /*
        mediaPlayer.release()
         */
    }
}