package com.ejebring.cf

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class User(val name: String, val passcode: String, val wins: Int = 0, val id: Int? = null) {
    constructor(login: Login) : this(login.username, login.passcode)
}

class UserService(database: Database) {
    object DBUsers : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val passcode = varchar("passcode", length = 50)
        val wins = integer("age")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(DBUsers)
        }
    }

    suspend fun create(user: User): Int = dbQuery {
        DBUsers.insert { dbUser ->
            dbUser[name] = user.name
            dbUser[passcode] = user.passcode
            dbUser[wins] = user.wins
        }[DBUsers.id]
    }

    suspend fun findById(id: Int): User? {
        return dbQuery {
            DBUsers.select { DBUsers.id eq id }
                .map { dbUser ->
                    User(
                        dbUser[DBUsers.name],
                        dbUser[DBUsers.passcode],
                        dbUser[DBUsers.wins],
                        dbUser[DBUsers.id]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun findByUsername(username: String): User? {
        return dbQuery {
            DBUsers.select { DBUsers.name eq username }
                .map { dbUser ->
                    User(
                        dbUser[DBUsers.name],
                        dbUser[DBUsers.passcode],
                        dbUser[DBUsers.wins],
                        dbUser[DBUsers.id]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: User) {
        dbQuery {
            DBUsers.update({ DBUsers.id eq id }) { dbUser ->
                dbUser[name] = user.name
                dbUser[wins] = user.wins
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            DBUsers.deleteWhere { DBUsers.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

