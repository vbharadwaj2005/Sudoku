package com.sudoku.game

import com.sudoku.game.audio.SoundManager
import com.sudoku.game.logic.ProgressManager
import com.sudoku.game.screens.MenuScreen
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class SudokuGame : Game() {
    lateinit var batch: SpriteBatch
        private set
    lateinit var font: BitmapFont
        private set
    lateinit var fontLarge: BitmapFont
        private set
    lateinit var fontSmall: BitmapFont
        private set

    val progress by lazy { ProgressManager.get() }

    companion object {
        val BG = Color(0.09f, 0.09f, 0.11f, 1f)
        val SURFACE = Color(0.13f, 0.13f, 0.16f, 1f)
        val SURFACE_VARIANT = Color(0.18f, 0.18f, 0.22f, 1f)
        val TEXT_PRIMARY = Color(0.98f, 0.98f, 0.98f, 1f)
        val TEXT_SECONDARY = Color(0.62f, 0.62f, 0.67f, 1f)
        val ACCENT = Color(0.58f, 0.40f, 0.98f, 1f)
        val SUCCESS = Color(0.34f, 0.80f, 0.55f, 1f)
        val DANGER = Color(0.94f, 0.33f, 0.31f, 1f)

        // Sudoku-specific tints built from the palette above.
        val CELL_BORDER = Color(0.20f, 0.20f, 0.24f, 1f)
        val SELECTED = Color(0.26f, 0.19f, 0.45f, 1f)
        val WRONG_CELL = Color(0.40f, 0.20f, 0.21f, 1f)

        val DECOR_COLORS = arrayOf(
            Color(0.58f, 0.40f, 0.98f, 1f),
            Color(0.34f, 0.80f, 0.55f, 1f),
            Color(0.96f, 0.62f, 0.20f, 1f),
            Color(0.23f, 0.66f, 0.95f, 1f),
            Color(0.94f, 0.33f, 0.31f, 1f),
            Color(0.96f, 0.87f, 0.24f, 1f),
            Color(0.83f, 0.33f, 0.75f, 1f),
            Color(0.20f, 0.83f, 0.83f, 1f),
        )
    }

    override fun create() {
        batch = SpriteBatch()
        font = FontHelper.createFont(22)
        fontLarge = FontHelper.createFont(56)
        fontSmall = FontHelper.createFont(16)
        SoundManager.init()
        SoundManager.isMuted = progress.isMuted
        setScreen(MenuScreen(this))
    }

    override fun dispose() {
        SoundManager.dispose()
        batch.dispose()
        font.dispose()
        fontLarge.dispose()
        fontSmall.dispose()
        super.dispose()
    }
}