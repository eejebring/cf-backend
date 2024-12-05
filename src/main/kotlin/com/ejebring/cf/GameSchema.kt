package com.ejebring.cf

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DBGame : Table() {
    val id = integer("id").autoIncrement()
    val redPlayer = varchar("red", length = 50) references DBUser.name
    val yellowPlayer = varchar("yellow", length = 50) references DBUser.name
    val redPlayedLast = bool("redPlayedLast")
    val isFinished = bool("isFinished")
    val board = varchar("board", length = 42)

    override val primaryKey = PrimaryKey(id)
}

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

        for (row in 0..5) {
            val index = row * 7 + column
            val currentPiece = if (redPlayedLast) 'r' else 'y'
            if (board[index] == 'n') {
                board = board.substring(0, index) + currentPiece + board.substring(index + 1)
                return
            }
        }
        redPlayedLast = !redPlayedLast
    }
}

class GameSchema(database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(DBGame)
        }
    }

    suspend fun create(game: Game): Int {
        return transaction {
            DBGame.insert {
                it[redPlayer] = game.redPlayer
                it[yellowPlayer] = game.yellowPlayer
                it[redPlayedLast] = game.redPlayedLast
                it[isFinished] = game.isFinished
                it[board] = game.board
            } get DBGame.id
        }
    }

    suspend fun getUserGames(username: String): List<Int> {
        return transaction {
            DBGame.selectAll()
                .where { (DBGame.redPlayer eq username) or (DBGame.yellowPlayer eq username) }
                .map {
                    it[DBGame.id]
                }
        }
    }

    suspend fun getGameById(id: Int): Game {
        return transaction {
            DBGame.selectAll()
                .where { DBGame.id eq id }
                .map {
                    Game(
                        it[DBGame.redPlayer],
                        it[DBGame.yellowPlayer],
                        it[DBGame.redPlayedLast],
                        it[DBGame.isFinished],
                        it[DBGame.board]
                    )
                }
                .single()
        }
    }

    suspend fun updateGame(game: Game, id: Int) {
        transaction {
            DBGame.update({ DBGame.id eq id }) {
                it[redPlayedLast] = game.redPlayedLast
                it[isFinished] = game.isFinished
                it[board] = game.board
            }
        }
    }
}