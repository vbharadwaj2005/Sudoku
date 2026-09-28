package com.sudoku.game.ui

import com.sudoku.game.SudokuGame
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders the 9x9 Sudoku board in the same visual language as the Arrows grid
 * renderer: rounded surface cells with subtle borders, accent 3x3 box
 * separators and a pulsing glow around the active cell.
 *
 * Because texts must be drawn with a SpriteBatch while the board itself uses
 * ShapeRenderer, drawing is split into a background pass and a digits pass.
 */
class BoardRenderer(
    private val shapeRenderer: ShapeRenderer,
    private val batch: Batch,
    private val digitFont: BitmapFont
) {
    var boardX = 0f
        private set
    var boardY = 0f
        private set
    var cellSize = 0f
        private set
    var gap = 3f

    private val radius: Float
        get() = min(cellSize * 0.14f, 9f)

    private val totalW: Float
        get() = SIZE * cellSize + gap * (SIZE + 1)

    private val totalH: Float
        get() = SIZE * cellSize + gap * (SIZE + 1)

    fun calculateLayout(rectX: Float, rectY: Float, rectW: Float, rectH: Float) {
        val innerW = rectW - gap * (SIZE + 1)
        val innerH = rectH - gap * (SIZE + 1)
        cellSize = min(innerW / SIZE, innerH / SIZE).coerceAtLeast(1f)
        boardX = rectX + (rectW - totalW) / 2f
        boardY = rectY + (rectH - totalH) / 2f
    }

    fun getCellScreenPos(row: Int, col: Int): Pair<Float, Float> {
        val x = boardX + gap + col * (cellSize + gap)
        val y = boardY + gap + (SIZE - 1 - row) * (cellSize + gap)
        return x to y
    }

    fun screenToCell(px: Float, py: Float): Int? {
        if (px < boardX || px > boardX + totalW || py < boardY || py > boardY + totalH) return null
        val col = ((px - boardX - gap) / (cellSize + gap)).toInt()
        val rowFromBottom = ((py - boardY - gap) / (cellSize + gap)).toInt()
        if (col !in 0 until SIZE || rowFromBottom !in 0 until SIZE) return null
        val row = SIZE - 1 - rowFromBottom
        return row * SIZE + col
    }

    fun drawBoardBackground(
        transientIndex: Int,
        highlights: IntArray,
        selected: Int,
        time: Float
    ) {
        if (selected in 0 until SIZE * SIZE) {
            val (sx, sy) = getCellScreenPos(selected / SIZE, selected % SIZE)
            val pulse = 0.20f + 0.10f * sin(time.toDouble() * 3.0).toFloat()
            shapeRenderer.color.set(SudokuGame.ACCENT.r, SudokuGame.ACCENT.g, SudokuGame.ACCENT.b, pulse)
            RoundedRect.draw(
                shapeRenderer, sx - SELECTED_GLOW, sy - SELECTED_GLOW,
                cellSize + SELECTED_GLOW * 2f, cellSize + SELECTED_GLOW * 2f,
                radius + SELECTED_GLOW
            )
        }

        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                val idx = row * SIZE + col
                val (x, y) = getCellScreenPos(row, col)

                shapeRenderer.color = SudokuGame.CELL_BORDER
                RoundedRect.draw(shapeRenderer, x, y, cellSize, cellSize, radius)

                val bg = when {
                    idx == transientIndex -> SudokuGame.WRONG_CELL
                    idx == selected -> SudokuGame.SELECTED
                    highlights[idx] != 0 -> SudokuGame.SURFACE_VARIANT
                    else -> SudokuGame.SURFACE
                }
                shapeRenderer.color = bg
                RoundedRect.draw(
                    shapeRenderer, x + CELL_INSET, y + CELL_INSET,
                    cellSize - CELL_INSET * 2f, cellSize - CELL_INSET * 2f,
                    (radius - CELL_INSET).coerceAtLeast(0f)
                )
            }
        }

        // 3x3 box separators in accent color, matching the web design.
        shapeRenderer.color = SudokuGame.ACCENT
        for (line in 1..2) {
            val col = line * 3 - 1
            val lineX = boardX + gap + (col + 1) * cellSize + col * gap
            shapeRenderer.rect(lineX, boardY, BOX_LINE_WIDTH, totalH)

            val row = line * 3 - 1
            val lineY = boardY + gap + (SIZE - 1 - row) * (cellSize + gap) + cellSize
            shapeRenderer.rect(boardX, lineY, totalW, BOX_LINE_WIDTH)
        }
    }

    fun drawBoardDigits(
        values: IntArray,
        prefilled: BooleanArray,
        transientIndex: Int,
        transientValue: Int
    ) {
        val gl = GlyphLayout()
        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                val idx = row * SIZE + col
                val value = if (idx == transientIndex) transientValue else values[idx]
                if (value == 0) continue

                val color = when {
                    idx == transientIndex -> SudokuGame.DANGER
                    prefilled[idx] -> SudokuGame.TEXT_PRIMARY
                    else -> SudokuGame.SUCCESS
                }
                val (x, y) = getCellScreenPos(row, col)
                val text = value.toString()
                digitFont.data.setScale((cellSize / 56f) * 0.80f)
                digitFont.color = color
                gl.setText(digitFont, text)
                digitFont.draw(
                    batch, text,
                    x + cellSize / 2f - gl.width / 2f,
                    y + cellSize / 2f + gl.height / 2f
                )
                digitFont.data.setScale(1f)
                digitFont.color.set(1f, 1f, 1f, 1f)
            }
        }
    }

    companion object {
        const val SIZE = 9
        private const val CELL_INSET = 1.2f
        private const val SELECTED_GLOW = 2.5f
        private const val BOX_LINE_WIDTH = 2f
    }
}