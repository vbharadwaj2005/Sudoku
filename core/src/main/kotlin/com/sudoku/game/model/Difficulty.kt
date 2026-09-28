package com.sudoku.game.model

/**
 * Difficulty levels with the number of starting clues for each,
 * matching the web version of the game.
 */
enum class Difficulty(val label: String, val clues: Int) {
    EASY("Easy", 40),
    MEDIUM("Medium", 30),
    HARD("Hard", 20);

    val buttonText: String get() = name

    companion object {
        const val MAX_MISTAKES = 3
    }
}