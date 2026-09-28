package com.sudoku.game.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle

object SoundManager {
    private var success: Sound? = null
    private var fail: Sound? = null
    private var complete: Sound? = null
    private var tap: Sound? = null
    private var initialized = false
    var isMuted = false

    fun init() {
        if (initialized) return
        try {
            val tempDir = Gdx.files.local("audio")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            success = loadSound(tempDir, "success.wav", WavGenerator.tone(880.0, 0.12))
            fail = loadSound(tempDir, "fail.wav", WavGenerator.tone(220.0, 0.18))
            complete = loadSound(
                tempDir, "complete.wav",
                WavGenerator.chord(listOf(523.25, 659.25, 783.99, 1046.5), 0.35)
            )
            tap = loadSound(tempDir, "tap.wav", WavGenerator.tone(440.0, 0.06))
            initialized = true
        } catch (e: Exception) {
            Gdx.app.error("SoundManager", "Failed to init audio", e)
        }
    }

    private fun loadSound(dir: FileHandle, name: String, wav: ByteArray): Sound? {
        return try {
            val file = dir.child(name)
            if (!file.exists()) {
                file.writeBytes(wav, false)
            }
            Gdx.audio.newSound(file)
        } catch (e: Exception) {
            null
        }
    }

    fun playSuccess() {
        if (!isMuted) success?.play(0.6f)
    }

    fun playFail() {
        if (!isMuted) fail?.play(0.7f)
    }

    fun playComplete() {
        if (!isMuted) complete?.play(0.8f)
    }

    fun playTap() {
        if (!isMuted) tap?.play(0.4f)
    }

    fun dispose() {
        success?.dispose()
        fail?.dispose()
        complete?.dispose()
        tap?.dispose()
        success = null
        fail = null
        complete = null
        tap = null
        initialized = false
    }
}