package com.sarftec.riddleme

import com.sarftec.riddleme.tools.SoundManager

interface GameListener {
    fun play()
    fun rate()
    fun nextRiddle()
    fun playSound(sound: SoundManager.Sound)
    fun gameCompleted()
}