package com.ejebring.cf

val rowStarts = intArrayOf(35, 28, 21, 14, 7, 0)
const val nrOfColumns = 7
const val nrOfRows = 6
val columnRange = 0..<nrOfColumns

// Board is a string of 42 characters, each representing a cell in the Connect Four board. y for yellow and r for red n for none.
class Game(

    val redPlayer: String,
    val yellowPlayer: String,
    var isRedTurn: Boolean = false,
    var winner: String = "TBD",
    var board: String = "n".repeat(42)
) {
    fun playMove(column: Int, playerName: String) {
        val currentPlayer = if (isRedTurn) redPlayer else yellowPlayer

        if (winner != "TBD") {
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

        for (row in rowStarts) {
            val index = row + column
            val currentPiece = if (isRedTurn) 'r' else 'y'
            if (board[index] == 'n') {
                board = board.substring(0, index) + currentPiece + board.substring(index + 1)
                checkWinCondition(index)
                isRedTurn = !isRedTurn
                return
            }
        }
    }

    private fun checkWinCondition(placedPiece: Int) {
        val placedColour = if (isRedTurn) "r" else "y"
        val winningPattern = placedColour.repeat(4)

        val winnableSlices = listOf(
            verticalSlicer(placedPiece),
            horizontalSlicer(placedPiece),
            positiveDiagonalSlicer(placedPiece),
            negativeDiagonalSlicer(placedPiece)
        )

        if (winnableSlices.any { it.contains(winningPattern) }) {
            winner = if (isRedTurn) redPlayer else yellowPlayer
        }
    }

    private fun verticalSlicer(placedPiece: Int): String {
        val column = placedPiece % nrOfColumns
        return rowStarts.map { board[it + column] }.joinToString("")
    }

    private fun horizontalSlicer(placedPiece: Int): String {
        val row = placedPiece - (placedPiece % nrOfColumns)
        return columnRange.map { board[row + it] }.joinToString("")
    }

    private fun positiveDiagonalSlicer(placedPiece: Int): String {
        val pieceColumn = placedPiece % nrOfColumns
        val pieceRow = placedPiece / nrOfColumns

        var diagonal = ""
        var cursor = pieceColumn + pieceRow + 1 - nrOfRows

        for (rowStart in rowStarts) {
            if ((0..(nrOfColumns - 1)).contains(cursor)) {
                diagonal += board.slice(rowStart..(rowStart + nrOfColumns - 1))[cursor]
            }
            cursor += 1
        }

        return diagonal
    }

    private fun negativeDiagonalSlicer(placedPiece: Int): String {
        val pieceColumn = placedPiece % nrOfColumns
        val pieceRow = placedPiece / nrOfColumns

        var diagonal = ""
        var cursor = pieceColumn - pieceRow - 1 + nrOfRows

        for (rowStart in rowStarts) {
            if ((0..(nrOfColumns - 1)).contains(cursor)) {
                diagonal += board.slice(rowStart..(rowStart + nrOfColumns - 1))[cursor]
            }
            cursor -= 1
        }

        return diagonal
    }
}