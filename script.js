const d = document;
const w = window;

const difficultySettings = {
    easy: 40,
    medium: 30,
    hard: 20
};

let difficulty, timerInterval, timeElapsed = 0, selectedCell = null, board = [], solution = [], mistakes = 0, isPaused = true, gameStarted = false;

const boardElement = d.getElementById('sudoku-board');
const numberPad = d.getElementById('number-pad');
const difficultyOverlay = d.getElementById('difficultyOverlay');
const gameContainer = d.getElementById('gameContainer');
const timerElement = d.getElementById('timer');
const winMessageElement = d.getElementById('winMessage');
const mistakesElement = d.getElementById('mistakes');
const startBtn = d.getElementById('startBtn');
const pauseBtn = d.getElementById('pauseBtn');
const pauseMessage = d.getElementById('pauseMessage');

function initializeGame(diff) {
    difficulty = diff;
    difficultyOverlay.classList.add('hidden');
    gameContainer.classList.remove('hidden');

    winMessageElement.textContent = "";
    boardElement.innerHTML = '';
    mistakes = 0;
    mistakesElement.textContent = `Mistakes: ${mistakes} / 3`;
    timeElapsed = 0;
    clearInterval(timerInterval);
    timerElement.textContent = '00:00';
    isPaused = true;
    gameStarted = false;
    startBtn.textContent = 'Start';
    startBtn.disabled = false;
    pauseBtn.textContent = 'Pause';
    pauseBtn.disabled = true;
    pauseMessage.classList.add('hidden');
    
    generatePuzzle();
    drawBoard();
    updateNumberPad();
}

function generatePuzzle() {
    board = Array.from({ length: 9 }, () => Array(9).fill(0));
    solveSudoku(board);
    solution = JSON.parse(JSON.stringify(board));
    let clues = difficultySettings[difficulty];
    let cells = Array.from({ length: 81 }, (_, i) => i);
    cells.sort(() => Math.random() - 0.5);
    for (let i = 0; i < 81 - clues; i++) {
        const row = Math.floor(cells[i] / 9);
        const col = cells[i] % 9;
        board[row][col] = 0;
    }
}

function drawBoard() {
    boardElement.innerHTML = '';
    for (let i = 0; i < 9; i++) {
        for (let j = 0; j < 9; j++) {
            const cell = d.createElement('div');
            cell.classList.add('cell');
            cell.dataset.row = i;
            cell.dataset.col = j;
            if (i > 0 && i % 3 === 0) cell.classList.add('row-boundary');
            const value = board[i][j];
            if (value !== 0) {
                cell.textContent = value;
                cell.classList.add('prefilled');
            }
            cell.addEventListener('click', () => handleCellClick(cell));
            boardElement.appendChild(cell);
        }
    }
}

function handleCellClick(cell) {
    if (isPaused || !cell) return;
    
    clearHighlights();
    
    selectedCell = cell;
    cell.classList.add('selected');
    
    const cellValue = cell.textContent;

    if (cellValue) {
        const row = parseInt(cell.dataset.row);
        const col = parseInt(cell.dataset.col);
        const boxRowStart = Math.floor(row / 3) * 3;
        const boxColStart = Math.floor(col / 3) * 3;

        for (let i = 0; i < 9; i++) {
            for (let j = 0; j < 9; j++) {
                const currentCell = boardElement.children[i * 9 + j];
                if (i === row || j === col || (i >= boxRowStart && i < boxRowStart + 3 && j >= boxColStart && j < boxColStart + 3)) {
                    currentCell.classList.add('highlighted');
                }
                if (currentCell.textContent === cellValue) {
                    currentCell.classList.add('highlighted');
                }
            }
        }
    }
}

function clearHighlights() {
    Array.from(boardElement.children).forEach(c => {
        c.classList.remove('selected', 'highlighted');
    });
}

function handleNumberInput(num) {
    if (isPaused) return;

    if (!selectedCell) {
        const firstEmptyCell = Array.from(boardElement.children).find(c => !c.textContent);
        if (firstEmptyCell) {
            handleCellClick(firstEmptyCell);
        } else {
            return;
        }
    }

    if (selectedCell.classList.contains('prefilled') || selectedCell.classList.contains('correct')) {
        return;
    }

    const row = parseInt(selectedCell.dataset.row);
    const col = parseInt(selectedCell.dataset.col);

    if (solution[row][col] === num) {
        board[row][col] = num;
        selectedCell.textContent = num;
        selectedCell.classList.add('user-input', 'correct');
        selectedCell.classList.remove('incorrect');
        updateNumberPad();
        checkWinCondition();
    } else {
        selectedCell.textContent = num;
        selectedCell.classList.add('incorrect');
        mistakes++;
        mistakesElement.textContent = `Mistakes: ${mistakes} / 3`;

        setTimeout(() => {
            selectedCell.textContent = '';
            selectedCell.classList.remove('incorrect');
        }, 500);

        if (mistakes >= 3) {
            endGame(false);
        }
    }
}

function checkWinCondition() {
    for (let i = 0; i < 9; i++) {
        for (let j = 0; j < 9; j++) {
            if (board[i][j] === 0 || board[i][j] !== solution[i][j]) {
                return;
            }
        }
    }
    endGame(true);
}

function endGame(isWin) {
    clearInterval(timerInterval);
    isPaused = true;
    startBtn.textContent = "Restart";
    startBtn.disabled = false;
    pauseBtn.disabled = true;

    if (isWin) {
        winMessageElement.textContent = `Solved!`;
        winMessageElement.style.color = 'var(--correct-color)';
    } else {
        winMessageElement.textContent = "Game Over!";
        winMessageElement.style.color = 'var(--error-color)';
    }
}

function handleStartClick() {
    if (startBtn.textContent === 'Restart') {
        initializeGame(difficulty);
        return;
    }
    
    gameStarted = true;
    isPaused = false;
    startBtn.textContent = 'Restart';
    pauseBtn.disabled = false;
    
    startTimer();
}

function startTimer() {
    timerInterval = setInterval(() => {
        timeElapsed++;
        const minutes = Math.floor(timeElapsed / 60);
        const seconds = timeElapsed % 60;
        timerElement.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    }, 1000);
}

function togglePause() {
    if (!gameStarted) return;
    isPaused = !isPaused;
    
    if (isPaused) {
        clearInterval(timerInterval);
        pauseBtn.textContent = 'Resume';
        pauseMessage.classList.remove('hidden');
    } else {
        startTimer();
        pauseBtn.textContent = 'Pause';
        pauseMessage.classList.add('hidden');
    }
}

function updateNumberPad() {
    for (let i = 1; i <= 9; i++) {
        let count = 0;
        for (let row = 0; row < 9; row++) {
            for (let col = 0; col < 9; col++) {
                if (board[row][col] === i) {
                    count++;
                }
            }
        }
        const btn = d.getElementById(`num-btn-${i}`);
        if (count === 9) {
            btn.classList.add('disabled');
            btn.disabled = true;
        } else {
            btn.classList.remove('disabled');
            btn.disabled = false;
        }
    }
}

function solveSudoku(board) {
    const find = findEmpty(board);
    if (!find) return true;
    const [row, col] = find;
    const nums = [1, 2, 3, 4, 5, 6, 7, 8, 9].sort(() => Math.random() - 0.5);
    for (let num of nums) {
        if (isValid(board, num, row, col)) {
            board[row][col] = num;
            if (solveSudoku(board)) return true;
            board[row][col] = 0;
        }
    }
    return false;
}

function findEmpty(board) {
    for (let i = 0; i < 9; i++) {
        for (let j = 0; j < 9; j++) {
            if (board[i][j] === 0) return [i, j];
        }
    }
    return null;
}

function isValid(board, num, row, col) {
    for (let i = 0; i < 9; i++) if (board[row][i] === num) return false;
    for (let i = 0; i < 9; i++) if (board[i][col] === num) return false;
    const boxX = Math.floor(col / 3) * 3;
    const boxY = Math.floor(row / 3) * 3;
    for (let i = boxY; i < boxY + 3; i++) {
        for (let j = boxX; j < boxX + 3; j++) {
            if (board[i][j] === num) return false;
        }
    }
    return true;
}

d.getElementById('easyBtn').addEventListener('click', () => initializeGame('easy'));
d.getElementById('mediumBtn').addEventListener('click', () => initializeGame('medium'));
d.getElementById('hardBtn').addEventListener('click', () => initializeGame('hard'));
startBtn.addEventListener('click', handleStartClick);
pauseBtn.addEventListener('click', togglePause);
d.getElementById('backButton').addEventListener('click', () => location.reload());

for (let i = 1; i <= 9; i++) {
    const btn = d.createElement('button');
    btn.textContent = i;
    btn.id = `num-btn-${i}`;
    btn.classList.add('num-btn');
    btn.addEventListener('click', () => handleNumberInput(i));
    numberPad.appendChild(btn);
}