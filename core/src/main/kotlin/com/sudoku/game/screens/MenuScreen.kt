package com.sudoku.game.screens

import com.sudoku.game.SudokuGame
import com.sudoku.game.audio.SoundManager
import com.sudoku.game.model.Difficulty
import com.sudoku.game.ui.Button
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.cos
import kotlin.math.sin

class MenuScreen(private val game: SudokuGame) : ScreenAdapter() {
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var easyButton: Button
    private lateinit var mediumButton: Button
    private lateinit var hardButton: Button
    private lateinit var soundButton: Button
    private var time = 0f

    override fun show() {
        shapeRenderer = ShapeRenderer()

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val btnW = w * 0.55f
        val btnH = 54f
        val centerX = w / 2f

        easyButton = Button(
            centerX - btnW / 2f, h * 0.30f,
            btnW, btnH,
            Difficulty.EASY.buttonText,
            game.font,
            SudokuGame.ACCENT,
            SudokuGame.TEXT_PRIMARY
        )

        mediumButton = Button(
            centerX - btnW / 2f, h * 0.30f - btnH - 14f,
            btnW, btnH,
            Difficulty.MEDIUM.buttonText,
            game.fontSmall,
            SudokuGame.SURFACE_VARIANT,
            SudokuGame.TEXT_PRIMARY
        )

        hardButton = Button(
            centerX - btnW / 2f, h * 0.30f - 2f * (btnH + 14f),
            btnW, btnH,
            Difficulty.HARD.buttonText,
            game.fontSmall,
            SudokuGame.SURFACE,
            SudokuGame.TEXT_PRIMARY
        )

        soundButton = Button(
            centerX - btnW / 2f, h * 0.30f - 3f * (btnH + 14f),
            btnW, btnH,
            soundLabel(),
            game.fontSmall,
            SudokuGame.SURFACE,
            SudokuGame.TEXT_SECONDARY
        )

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val x = screenX.toFloat()
                val y = (Gdx.graphics.height - screenY).toFloat()

                when {
                    easyButton.contains(x, y) -> {
                        easyButton.press()
                        SoundManager.playTap()
                        startGame(Difficulty.EASY)
                    }
                    mediumButton.contains(x, y) -> {
                        mediumButton.press()
                        SoundManager.playTap()
                        startGame(Difficulty.MEDIUM)
                    }
                    hardButton.contains(x, y) -> {
                        hardButton.press()
                        SoundManager.playTap()
                        startGame(Difficulty.HARD)
                    }
                    soundButton.contains(x, y) -> {
                        soundButton.press()
                        SoundManager.isMuted = !SoundManager.isMuted
                        game.progress.isMuted = SoundManager.isMuted
                        soundButton.text = soundLabel()
                    }
                }
                return true
            }
        }
    }

    private fun startGame(difficulty: Difficulty) {
        game.progress.lastDifficulty = difficulty.name
        game.setScreen(GameScreen(game, difficulty))
    }

    private fun soundLabel(): String = if (SoundManager.isMuted) "SOUND: OFF" else "SOUND: ON"

    override fun render(delta: Float) {
        time += delta

        Gdx.gl.glClearColor(SudokuGame.BG.r, SudokuGame.BG.g, SudokuGame.BG.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)

        easyButton.update(delta)
        mediumButton.update(delta)
        hardButton.update(delta)
        soundButton.update(delta)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawDecorativeDots(w, h)
        easyButton.drawBackground(shapeRenderer)
        mediumButton.drawBackground(shapeRenderer)
        hardButton.drawBackground(shapeRenderer)
        soundButton.drawBackground(shapeRenderer)
        shapeRenderer.end()

        game.batch.begin()
        drawTitle(w, h)
        easyButton.drawText(game.batch)
        mediumButton.drawText(game.batch)
        hardButton.drawText(game.batch)
        soundButton.drawText(game.batch)
        drawFooter(w, h)
        game.batch.end()
    }

    private fun drawTitle(w: Float, h: Float) {
        val pulse = (1.0 + sin(time.toDouble() * 1.6) * 0.012).toFloat()
        game.fontLarge.data.setScale(0.74f * pulse)
        game.fontLarge.color = SudokuGame.TEXT_PRIMARY
        val titleLayout = GlyphLayout(game.fontLarge, "SUDOKU")
        game.fontLarge.draw(game.batch, "SUDOKU", w / 2f - titleLayout.width / 2f, h * 0.74f)
        game.fontLarge.data.setScale(1f)

        game.fontSmall.color = SudokuGame.TEXT_SECONDARY
        val subtitleLayout = GlyphLayout(game.fontSmall, "fill the grid")
        game.fontSmall.draw(game.batch, "fill the grid", w / 2f - subtitleLayout.width / 2f, h * 0.74f - titleLayout.height - 10f)

        val difficultyName = runCatching {
            Difficulty.valueOf(game.progress.lastDifficulty)
        }.getOrDefault(Difficulty.EASY)
        val best = game.progress.bestTime(difficultyName)
        val bestText = if (best < 0) "Best: --:--" else "Best: ${formatTime(best)}"
        val bestLayout = GlyphLayout(game.fontSmall, bestText)
        game.fontSmall.color = SudokuGame.TEXT_SECONDARY
        game.fontSmall.draw(game.batch, bestText, w / 2f - bestLayout.width / 2f, h * 0.46f)
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    private fun drawFooter(w: Float, h: Float) {
        val hintLayout = GlyphLayout(game.fontSmall, "Fill every row, column, and 3x3 box with 1-9")
        game.fontSmall.color = SudokuGame.TEXT_SECONDARY
        game.fontSmall.draw(
            game.batch, "Fill every row, column, and 3x3 box with 1-9",
            w / 2f - hintLayout.width / 2f, 30f
        )
    }

    private fun drawDecorativeDots(w: Float, h: Float) {
        val anchorY = h * 0.80f
        val count = 10

        for (i in 0 until count) {
            val angle = time.toDouble() * 0.25 + i * (Math.PI * 2.0 / count)
            val radius = 40.0 + 90.0 * i.toDouble() / count
            val ax = (w / 2.0 + cos(angle) * radius).toFloat()
            val ay = (anchorY.toDouble() + sin(angle * 0.7) * radius * 0.18).toFloat()
            val dot = 4f + 2f * i.toFloat() / count

            val color = SudokuGame.DECOR_COLORS[i % SudokuGame.DECOR_COLORS.size]
            val alpha = (0.12 + 0.08 * ((sin(time.toDouble() + i) + 1.0) / 2.0)).toFloat()
            shapeRenderer.color.set(color.r, color.g, color.b, alpha)
            shapeRenderer.circle(ax, ay, dot)
        }
    }

    override fun resize(width: Int, height: Int) {
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun hide() {
        shapeRenderer.dispose()
    }
}