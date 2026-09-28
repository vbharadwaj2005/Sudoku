package com.sudoku.game.logic

import com.sudoku.game.model.Difficulty
import com.sudoku.game.model.SudokuBoard

/**
 * Generates Sudoku puzzles using the exact algorithm from the web version:
 * fill a complete grid with a randomized backtracking solver, then remove
 * cells until only the chosen number of clues remains.
 */
object PuzzleGenerator {

    private val digits = (1..9).toList()

    fun generate(difficulty: Difficulty): SudokuBoard {
        val board = SudokuBoard()

        solveFrom(board.solution)

        board.solution.copyInto(board.values)

        val cells = (0 until SudokuBoard.CELLS).shuffled()
        val clues = difficulty.clues
        for (i in clues until SudokuBoard.CELLS) {
            board.values[cells[i]] = 0
        }

        for (i in 0 until SudokuBoard.CELLS) {
            board.prefilled[i] = board.values[i] != 0
        }

        return board
    }

    /**
     * Randomized backtracking solver. Returns true when the grid is fully
     * solved. Mirrors `solveSudoku` from the web version.
     */
    private fun solveFrom(grid: IntArray): Boolean {
        val empty = findEmpty(grid) ?: return true
        for (num in digits.shuffled()) {
            if (isValid(grid, num, empty)) {
                grid[empty] = num
                if (solveFrom(grid)) return true
                grid[empty] = 0
            }
        }
        return false
    }

    fun findEmpty(grid: IntArray): Int? {
        for (i in grid.indices) {
            if (grid[i] == 0) return i
        }
        return null
    }

    fun isValid(grid: IntArray, num: Int, index: Int): Boolean {
        val row = index / SudokuBoard.SIZE
        val col = index % SudokuBoard.SIZE

        for (c in 0 until SudokuBoard.SIZE) {
            if (grid[row * SudokuBoard.SIZE + c] == num) return false
        }
        for (r in 0 until SudokuBoard.SIZE) {
            if (grid[r * SudokuBoard.SIZE + col] == num) return false
        }

        val boxRowStart = (row / 3) * 3
        val boxColStart = (col / 3) * 3
        for (r in boxRowStart until boxRowStart + 3) {
            for (c in boxColStart until boxColStart + 3) {
                if (grid[r * SudokuBoard.SIZE + c] == num) return false
            }
        }
        return true
    }
}