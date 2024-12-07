package com.ejebring.cf

val rowStarts = intArrayOf(35, 28, 21, 14, 7, 0)
const val nrOfColumns = 7
const val nrOfRows = 6
val columnRange = 0..nrOfColumns

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

        for (row in rowStarts) {
            val index = row + column
            val currentPiece = if (redPlayedLast) 'r' else 'y'
            if (board[index] == 'n') {
                board = board.substring(0, index) + currentPiece + board.substring(index + 1)
                redPlayedLast = !redPlayedLast
                checkWinCondition(index)
                return
            }
        }
    }

    private fun checkWinCondition(placedPiece: Int) {

        val verticalSlice = verticalSlicer(placedPiece)
        val horizontalSlice = horizontalSlicer(placedPiece)
        val positiveDiagonalSlice = positiveDiagonalSlicer(placedPiece)
        val negativeDiagonalSlice = negativeDiagonalSlicer(placedPiece)

        println(verticalSlice)
        println(horizontalSlice)
        println(positiveDiagonalSlice)
        println(negativeDiagonalSlice)
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
            if (cursor <= 0 || cursor > nrOfColumns) {
                cursor += 1
                continue
            }
            diagonal += board.slice(rowStart..(rowStart + nrOfColumns - 1))[cursor]
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
            if (cursor < 0 || cursor >= nrOfColumns) {
                cursor -= 1
                continue
            }
            diagonal += board.slice(rowStart..(rowStart + nrOfColumns - 1))[cursor]
            cursor -= 1
        }

        return diagonal
    }
}