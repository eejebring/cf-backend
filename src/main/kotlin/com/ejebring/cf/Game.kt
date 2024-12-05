package com.ejebring.cf

// Board is a string of 42 characters, each representing a cell in the Connect Four board. y for yellow and r for red n for none.
class Game(
    val redPlayer: String,
    val yellowPlayer: String,
    var redPlayedLast: Boolean = true,
    var isFinished: Boolean = false,
    var board: String = "n".repeat(42)
) {
    fun playMove(column: Int, playerName: String) {
        val currentPlayer = if (redPlayedLast) yellowPlayer else redPlayer

        if (isFinished) {
            throw IllegalArgumentException("Game is already finished")
        }
        if (playerName != currentPlayer) {
            throw IllegalArgumentException("It is not your turn")
        }
        if (column < 0 || column > 6) {
            throw IllegalArgumentException("Column must be between 0 and 6")
        }
        if (board[column] != 'n') {
            throw IllegalArgumentException("Column is full")
        }

        for (row in intArrayOf(35, 28, 21, 14, 7, 0)) {
            val index = row + column
            val currentPiece = if (redPlayedLast) 'r' else 'y'
            if (board[index] == 'n') {
                board = board.substring(0, index) + currentPiece + board.substring(index + 1)
                redPlayedLast = !redPlayedLast
                return
            }
        }
    }
}