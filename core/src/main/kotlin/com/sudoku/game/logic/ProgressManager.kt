package com.sudoku.game.logic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.sudoku.game.model.Difficulty

/**
 * Persists lightweight player preferences (sound toggle, best solve times)
 * using the same libGDX Preferences storage the Arrows app uses.
 */
class ProgressManager private constructor() {
    private val prefs: Preferences = Gdx.app.getPreferences(STORAGE_NAME)

    var isMuted: Boolean
        get() = prefs.getBoolean("muted", false)
        set(value) {
            if (value != isMuted) {
                prefs.putBoolean("muted", value)
                prefs.flush()
            }
        }

    var lastDifficulty: String
        get() = prefs.getString("lastDifficulty", Difficulty.EASY.name)
        set(value) {
            if (value != lastDifficulty) {
                prefs.putString("lastDifficulty", value)
                prefs.flush()
            }
        }

    fun bestTime(difficulty: Difficulty): Long =
        prefs.getLong("bestTime_" + difficulty.name, -1L)

    fun recordTime(difficulty: Difficulty, seconds: Int): Boolean {
        val current = prefs.getLong("bestTime_" + difficulty.name, Long.MAX_VALUE)
        if (seconds < current) {
            prefs.putLong("bestTime_" + difficulty.name, seconds.toLong())
            prefs.flush()
            return true
        }
        return false
    }

    fun reset() {
        prefs.clear()
        prefs.flush()
    }

    companion object {
        private const val STORAGE_NAME = "sudoku-progress"

        private val instance: ProgressManager by lazy { ProgressManager() }

        fun get(): ProgressManager = instance
    }
}