# Sudoku

A classic 9×9 Sudoku puzzle game with procedurally generated puzzles and a dark, minimal interface.

## How to Play

- Tap an empty cell, then tap a number (1–9) to fill it.
- Every row, column, and 3×3 box must contain the digits 1–9 exactly once.
- You have 3 lives — each wrong answer costs one. Three mistakes ends the game.

## Difficulty Levels

| Difficulty | Starting Clues |
|---|---|
| Easy | 40 |
| Medium | 30 |
| Hard | 20 |

## Features

- **Procedural puzzle generation** via randomized backtracking solver
- Timer tracking with best time persistence per difficulty
- Visual highlighting of related row, column, box, and matching numbers
- Numpad disables digits that are already placed 9 times
- Restart generates a fresh puzzle for the same difficulty
- Pause/resume with timer toggle
- Procedurally generated sound effects (no audio assets)
- Dark theme with animated menu

## Tech Stack

| Component | Detail |
|---|---|
| Language | Kotlin 1.9.22 |
| Framework | libGDX 1.12.1 (LWJGL3 backend) |
| Build | Gradle 8.5 (Kotlin DSL) |
| Font | Inter-Regular (FreeType) |
| Platform | Desktop JVM |

## Getting Started

### Prerequisites

- JDK 8 or newer
- Gradle 8.5 (wrapper included)

### Run

```bash
./gradlew :core:run
```

### Build JAR

```bash
./gradlew :core:build
java -jar core/build/libs/core-1.0.0.jar
```

## Controls

| Input | Action |
|---|---|
| Tap cell | Select cell for input |
| Tap number pad | Fill selected cell with that digit |
| Back button | Return to menu |
| Start | Begin a new game |
| Restart | Generate a new puzzle |
| Pause | Pause / resume the timer |
