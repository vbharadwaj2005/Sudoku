package com.sudoku.game

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Sudoku")
    config.setWindowedMode(420, 750)
    config.setResizable(false)
    config.useVsync(true)
    config.setIdleFPS(60)
    Lwjgl3Application(SudokuGame(), config)
}