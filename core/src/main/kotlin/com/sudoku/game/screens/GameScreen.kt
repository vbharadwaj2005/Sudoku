package com.sudoku.game.screens

import com.sudoku.game.SudokuGame
import com.sudoku.game.audio.SoundManager
import com.sudoku.game.logic.PuzzleGenerator
import com.sudoku.game.model.Difficulty
import com.sudoku.game.model.SudokuBoard
import com.sudoku.game.ui.BoardRenderer
import com.sudoku.game.ui.Button
import com.sudoku.game.ui.RoundedRect
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.sin

/**
 * The Sudoku board screen. Gameplay mirrors the web version exactly:
 *   3 difficulty levels, a 3-mistake limit, row/column/box/same-number
 *   highlighting, a dynamic number pad, a timer with pause/resume and
 *   Start/Restart flow, styled with the Arrows theme.
 */
class GameScreen(private val game: SudokuGame, private val difficulty: Difficulty) : ScreenAdapter() {

    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var boardRenderer: BoardRenderer

    private lateinit var board: SudokuBoard
    private val correct = BooleanArray(SudokuBoard.CELLS)
    private val highlights = IntArray(SudokuBoard.CELLS)
    private var selected = -1

    private var transientIndex = -1
    private var transientValue = 0
    private var transientTimer = 0f

    private var mistakes = 0
    private var timeElapsed = 0f
    private var time = 0f

    private var gameStarted = false
    private var isPaused = true
    private var showSolved = false
    private var solvedTimer = 0f
    private var showFailed = false
    private var failedTimer = 0f

    private lateinit var backButton: Button
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var numPadButtons: Array<Button>

    // ---- Lifecycle -------------------------------------------------------

    override fun show() {
        shapeRenderer = ShapeRenderer()
        boardRenderer = BoardRenderer(shapeRenderer, game.batch, game.fontLarge)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        createButtons(w, h)
        layoutButtons(w, h)

        boardRenderer.calculateLayout(boardRectX(w), boardRectY(), boardAreaW(w), boardAreaH(h))

        initializeGame()

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (showSolved || showFailed) return false

                val x = screenX.toFloat()
                val y = (Gdx.graphics.height - screenY).toFloat()

                when {
                    backButton.contains(x, y) -> {
                        backButton.press()
                        SoundManager.playTap()
                        game.setScreen(MenuScreen(game))
                    }
                    startButton.contains(x, y) -> {
                        startButton.press()
                        SoundManager.playTap()
                        handleStartClick()
                    }
                    pauseButton.contains(x, y) -> {
                        pauseButton.press()
                        SoundManager.playTap()
                        togglePause()
                    }
                    else -> {
                        if (!handleNumPadTap(x, y)) {
                            boardRenderer.screenToCell(x, y)?.let { handleCellClick(it) }
                        }
                    }
                }
                return true
            }
        }
    }

    // ---- Layout ----------------------------------------------------------

    private fun boardAreaW(w: Float): Float = minOf(w - 28f, 380f)
    private fun boardAreaH(h: Float): Float = (h - TOP_GAP - BOTTOM_GAP).coerceAtLeast(200f)
    private fun boardRectX(w: Float): Float = (w - boardAreaW(w)) / 2f
    private fun boardRectY(): Float = BOTTOM_GAP

    private fun createButtons(w: Float, h: Float) {
        backButton = Button(
            0f, 0f, 58f, 28f, "BACK", game.fontSmall,
            SudokuGame.SURFACE_VARIANT, SudokuGame.TEXT_PRIMARY, 8f
        )

        startButton = Button(
            0f, 0f, 150f, 44f, "START", game.font,
            SudokuGame.ACCENT, SudokuGame.TEXT_PRIMARY
        )
        startButton.textScale = 0.92f

        pauseButton = Button(
            0f, 0f, 150f, 44f, "PAUSE", game.font,
            SudokuGame.SURFACE_VARIANT, SudokuGame.TEXT_PRIMARY
        )
        pauseButton.textScale = 0.92f

        numPadButtons = Array(9) { i ->
            Button(
                0f, 0f, NUM_PAD_BUTTON_W, NUM_PAD_BUTTON_H, (i + 1).toString(), game.font,
                SudokuGame.SURFACE_VARIANT, SudokuGame.TEXT_PRIMARY, 10f, 1.1f
            )
        }
    }

    private fun layoutButtons(w: Float, h: Float) {
        backButton.x = 12f
        backButton.y = h - 40f

        val rowY = START_BUTTONS_Y
        startButton.x = (w - START_ROW_WIDTH) / 2f
        startButton.y = rowY
        pauseButton.x = startButton.x + startButton.width + START_BUTTONS_GAP
        pauseButton.y = rowY

        val padTotal = 9 * NUM_PAD_BUTTON_W + 8 * NUM_PAD_GAP
        for (i in 0 until 9) {
            numPadButtons[i].x = (w - padTotal) / 2f + i * (NUM_PAD_BUTTON_W + NUM_PAD_GAP)
            numPadButtons[i].y = NUM_PAD_Y
        }
    }

    // ---- Game state management ------------------------------------------

    private fun initializeGame() {
        board = PuzzleGenerator.generate(difficulty)
        correct.fill(false)
        highlights.fill(0)
        selected = -1
        transientIndex = -1
        transientValue = 0
        transientTimer = 0f
        mistakes = 0
        timeElapsed = 0f
        gameStarted = false
        isPaused = true
        showSolved = false
        solvedTimer = 0f
        showFailed = false
        failedTimer = 0f

        startButton.text = "START"
        startButton.isEnabled = true
        pauseButton.text = "PAUSE"
        pauseButton.isEnabled = false

        updateNumberPad()
    }

    private fun handleStartClick() {
        if (startButton.text == "RESTART") {
            initializeGame()
            return
        }

        gameStarted = true
        isPaused = false
        startButton.text = "RESTART"
        pauseButton.isEnabled = true
    }

    private fun togglePause() {
        if (!gameStarted) return
        isPaused = !isPaused
        pauseButton.text = if (isPaused) "RESUME" else "PAUSE"
    }

    private fun handleCellClick(idx: Int) {
        if (isPaused || idx !in 0 until SudokuBoard.CELLS) return
        highlights.fill(0)
        selected = idx
        updateHighlights(idx)
    }

    private fun updateHighlights(idx: Int) {
        val value = displayedValue(idx)
        if (value == 0) return

        val row = idx / SudokuBoard.SIZE
        val col = idx % SudokuBoard.SIZE
        val boxRowStart = (row / 3) * 3
        val boxColStart = (col / 3) * 3

        for (r in 0 until SudokuBoard.SIZE) {
            for (c in 0 until SudokuBoard.SIZE) {
                val inBox = r >= boxRowStart && r < boxRowStart + 3 && c >= boxColStart && c < boxColStart + 3
                if (r == row || c == col || inBox || displayedValue(r * SudokuBoard.SIZE + c) == value) {
                    highlights[r * SudokuBoard.SIZE + c] = 1
                }
            }
        }
    }

    private fun displayedValue(idx: Int): Int =
        if (idx == transientIndex) transientValue else board.values[idx]

    private fun handleNumPadTap(x: Float, y: Float): Boolean {
        for (i in numPadButtons.indices) {
            if (numPadButtons[i].contains(x, y)) {
                numPadButtons[i].press()
                handleNumberInput(i + 1)
                return true
            }
        }
        return false
    }

    private fun handleNumberInput(num: Int) {
        if (isPaused) return
        if (transientIndex >= 0) return

        var idx = selected
        if (idx == -1) {
            idx = findFirstEmpty() ?: return
            selected = idx
            highlights.fill(0)
        }

        if (board.prefilled[idx] || correct[idx]) return

        if (board.solution[idx] == num) {
            board.set(idx, num)
            correct[idx] = true
            SoundManager.playSuccess()
            updateNumberPad()
            checkWinCondition()
        } else {
            transientIndex = idx
            transientValue = num
            transientTimer = WRONG_FLASH_SECONDS
            mistakes++
            SoundManager.playFail()
            if (mistakes >= Difficulty.MAX_MISTAKES) {
                endGame(false)
            }
        }
    }

    private fun findFirstEmpty(): Int? {
        for (i in 0 until SudokuBoard.CELLS) {
            if (board.values[i] == 0) return i
        }
        return null
    }

    private fun updateNumberPad() {
        for (i in 0 until 9) {
            numPadButtons[i].isEnabled = board.countDigit(i + 1) < 9
        }
    }

    private fun checkWinCondition() {
        if (board.isComplete) {
            endGame(true)
        }
    }

    private fun endGame(isWin: Boolean) {
        isPaused = true
        gameStarted = false
        startButton.text = "RESTART"
        startButton.isEnabled = true
        pauseButton.isEnabled = false

        if (isWin) {
            SoundManager.playComplete()
            game.progress.recordTime(difficulty, timeElapsed.toInt())
            showSolved = true
            solvedTimer = 0f
        } else {
            showFailed = true
            failedTimer = 0f
        }
    }

    // ---- Frame update / render ------------------------------------------

    override fun render(delta: Float) {
        time += delta

        if (transientIndex >= 0) {
            transientTimer -= delta
            if (transientTimer <= 0f) {
                transientIndex = -1
                transientValue = 0
            }
        }

        if (gameStarted && !isPaused) {
            timeElapsed += delta
        }

        if (showSolved) {
            solvedTimer += delta
            if (solvedTimer > SOLVED_OVERLAY_TIME) showSolved = false
        }
        if (showFailed) {
            failedTimer += delta
            if (failedTimer > FAILED_OVERLAY_TIME) showFailed = false
        }

        backButton.update(delta)
        startButton.update(delta)
        pauseButton.update(delta)
        for (btn in numPadButtons) btn.update(delta)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        Gdx.gl.glClearColor(SudokuGame.BG.r, SudokuGame.BG.g, SudokuGame.BG.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        boardRenderer.drawBoardBackground(transientIndex, highlights, selected, time)
        drawProgressBar(w, h)
        backButton.drawBackground(shapeRenderer)
        startButton.drawBackground(shapeRenderer)
        pauseButton.drawBackground(shapeRenderer)
        for (btn in numPadButtons) btn.drawBackground(shapeRenderer)
        shapeRenderer.end()

        game.batch.begin()
        boardRenderer.drawBoardDigits(board.values, board.prefilled, transientIndex, transientValue)
        drawHUDText(w, h)
        backButton.drawText(game.batch)
        startButton.drawText(game.batch)
        pauseButton.drawText(game.batch)
        for (btn in numPadButtons) btn.drawText(game.batch)
        game.batch.end()

        if (showSolved) drawSolvedOverlay(w, h)
        if (showFailed) drawFailedOverlay(w, h)
    }

    private fun drawProgressBar(w: Float, h: Float) {
        val barY = h - 47f
        val barW = w - 28f
        val progress = board.filledCount.toFloat() / SudokuBoard.CELLS

        shapeRenderer.color = SudokuGame.SURFACE
        RoundedRect.draw(shapeRenderer, 14f, barY, barW, barH(), 2.5f)
        if (progress > 0f) {
            shapeRenderer.color = SudokuGame.ACCENT
            RoundedRect.draw(shapeRenderer, 14f, barY, barW * progress, barH(), 2.5f)
        }
    }

    private fun barH(): Float = 4f

    private fun drawHUDText(w: Float, h: Float) {
        val textY = h - 68f

        game.fontSmall.color = SudokuGame.TEXT_SECONDARY
        game.fontSmall.draw(game.batch, difficulty.label.uppercase(), 14f, textY)

        val timerText = formatTime(timeElapsed.toInt())
        val timerLayout = GlyphLayout(game.font, timerText)
        game.font.color = SudokuGame.TEXT_PRIMARY
        game.font.draw(game.batch, timerText, w / 2f - timerLayout.width / 2f, textY)

        val remaining = Difficulty.MAX_MISTAKES - mistakes
        val heartsText = if (remaining > 0) "\u2665".repeat(remaining) else "X"
        game.font.color = if (remaining > 0) SudokuGame.DANGER else SudokuGame.TEXT_SECONDARY
        val heartsLayout = GlyphLayout(game.font, heartsText)
        game.font.draw(game.batch, heartsText, w - 14f - heartsLayout.width, textY)

        // Small centered title with paused indicator, like the web top bar.
        game.fontLarge.data.setScale(0.30f)
        game.fontLarge.color = SudokuGame.TEXT_PRIMARY
        val titleText = "SUDOKU"
        val titleLayout = GlyphLayout(game.fontLarge, titleText)
        val titleX = w / 2f - titleLayout.width / 2f
        game.fontLarge.draw(game.batch, titleText, titleX, h - 18f)
        game.fontLarge.data.setScale(1f)

        if (isPaused && gameStarted) {
            game.fontSmall.color = SudokuGame.DANGER
            game.fontSmall.draw(game.batch, "Paused", titleX + titleLayout.width + 12f, h - 16f)
        }
    }

    private fun drawSolvedOverlay(w: Float, h: Float) {
        val alpha = (solvedTimer / 1.5f).coerceIn(0f, 1f)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color.set(SudokuGame.BG.r, SudokuGame.BG.g, SudokuGame.BG.b, alpha * 0.88f)
        shapeRenderer.rect(0f, 0f, w, h)

        val pop = (solvedTimer.toDouble() * 2.0).coerceAtMost(Math.PI)
        val scale = 0.6f + 0.4f * sin(pop).toFloat()
        val radius = 38f * scale
        if (radius > 1f) {
            shapeRenderer.color.set(SudokuGame.SUCCESS.r, SudokuGame.SUCCESS.g, SudokuGame.SUCCESS.b, alpha)
            shapeRenderer.circle(w / 2f, h * 0.58f, radius)
        }
        shapeRenderer.end()

        game.batch.begin()
        game.fontLarge.color.set(SudokuGame.TEXT_PRIMARY.r, SudokuGame.TEXT_PRIMARY.g, SudokuGame.TEXT_PRIMARY.b, alpha)
        game.fontLarge.data.setScale(0.85f)
        val solvedLayout = GlyphLayout(game.fontLarge, "SOLVED!")
        game.fontLarge.draw(game.batch, "SOLVED!", w / 2f - solvedLayout.width / 2f, h * 0.68f)
        game.fontLarge.data.setScale(1f)

        game.font.color.set(SudokuGame.SUCCESS.r, SudokuGame.SUCCESS.g, SudokuGame.SUCCESS.b, alpha)
        val timeText = "Time: ${formatTime(timeElapsed.toInt())}"
        val timeLayout = GlyphLayout(game.font, timeText)
        game.font.draw(game.batch, timeText, w / 2f - timeLayout.width / 2f, h * 0.58f)

        if (solvedTimer > 0.6f) {
            val fadeIn = ((solvedTimer - 0.6f) / 0.5f).coerceIn(0f, 1f)
            game.fontSmall.color.set(SudokuGame.TEXT_SECONDARY.r, SudokuGame.TEXT_SECONDARY.g, SudokuGame.TEXT_SECONDARY.b, fadeIn)
            val restartText = "Tap Restart to play again"
            val restartLayout = GlyphLayout(game.fontSmall, restartText)
            game.fontSmall.draw(game.batch, restartText, w / 2f - restartLayout.width / 2f, h * 0.5f)
        }
        game.batch.end()
    }

    private fun drawFailedOverlay(w: Float, h: Float) {
        val alpha = (failedTimer / 1.5f).coerceIn(0f, 1f)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color.set(SudokuGame.BG.r, SudokuGame.BG.g, SudokuGame.BG.b, alpha * 0.88f)
        shapeRenderer.rect(0f, 0f, w, h)

        val pop = (failedTimer.toDouble() * 2.0).coerceAtMost(Math.PI)
        val scale = 0.6f + 0.4f * sin(pop).toFloat()
        val radius = 38f * scale
        if (radius > 1f) {
            shapeRenderer.color.set(SudokuGame.DANGER.r, SudokuGame.DANGER.g, SudokuGame.DANGER.b, alpha)
            shapeRenderer.circle(w / 2f, h * 0.58f, radius)
        }
        shapeRenderer.end()

        game.batch.begin()
        game.fontLarge.color.set(SudokuGame.TEXT_PRIMARY.r, SudokuGame.TEXT_PRIMARY.g, SudokuGame.TEXT_PRIMARY.b, alpha)
        game.fontLarge.data.setScale(0.85f)
        val failLayout = GlyphLayout(game.fontLarge, "GAME OVER")
        game.fontLarge.draw(game.batch, "GAME OVER", w / 2f - failLayout.width / 2f, h * 0.68f)
        game.fontLarge.data.setScale(1f)

        if (failedTimer > 0.6f) {
            val fadeIn = ((failedTimer - 0.6f) / 0.5f).coerceIn(0f, 1f)
            game.fontSmall.color.set(SudokuGame.TEXT_SECONDARY.r, SudokuGame.TEXT_SECONDARY.g, SudokuGame.TEXT_SECONDARY.b, fadeIn)
            val retryText = "Tap Restart to try again"
            val retryLayout = GlyphLayout(game.fontSmall, retryText)
            game.fontSmall.draw(game.batch, retryText, w / 2f - retryLayout.width / 2f, h * 0.52f)
        }
        game.batch.end()
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun resize(width: Int, height: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        layoutButtons(w, h)
        boardRenderer.calculateLayout(boardRectX(w), boardRectY(), boardAreaW(w), boardAreaH(h))
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
    }

    override fun hide() {
        shapeRenderer.dispose()
    }

    companion object {
        private const val TOP_GAP = 84f
        private const val BOTTOM_GAP = 132f
        private const val NUM_PAD_Y = 18f
        private const val NUM_PAD_GAP = 6f
        private const val NUM_PAD_BUTTON_W = 36f
        private const val NUM_PAD_BUTTON_H = 44f
        private const val START_BUTTONS_Y = 76f
        private const val START_BUTTONS_GAP = 20f
        private const val START_ROW_WIDTH = 150f * 2f + START_BUTTONS_GAP
        private const val WRONG_FLASH_SECONDS = 0.5f
        private const val SOLVED_OVERLAY_TIME = 1.5f
        private const val FAILED_OVERLAY_TIME = 1.8f
    }
}