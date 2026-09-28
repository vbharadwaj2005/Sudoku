package com.sudoku.game.model

/**
 * Simple 9x9 Sudoku board backed by flat arrays.
 *
 * [values] holds the permanent board state: starting clues plus the
 * numbers the player has placed correctly. 0 means an empty cell.
 *
 * [solution] holds the fully solved grid the puzzle was generated from.
 *
 * [prefilled] marks the cells that were given as clues (not editable).
 */
class SudokuBoard {
    val values = IntArray(CELLS)
    val solution = IntArray(CELLS)
    val prefilled = BooleanArray(CELLS)

    val isComplete: Boolean
        get() = values.none { it == 0 }

    val filledCount: Int
        get() {
            var count = 0
            for (v in values) {
                if (v != 0) count++
            }
            return count
        }

    fun set(index: Int, value: Int) {
        values[index] = value
    }

    fun countDigit(digit: Int): Int {
        var count = 0
        for (i in 0 until CELLS) {
            if (values[i] == digit) count++
        }
        return count
    }

    companion object {
        const val SIZE = 9
        const val CELLS = SIZE * SIZE
    }
}