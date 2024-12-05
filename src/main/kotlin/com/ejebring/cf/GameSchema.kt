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