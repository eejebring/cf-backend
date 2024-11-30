package com.ejebring.cf

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class User(val name: String, val passcode: String, val wins: Int, val id: Int, val updatedAt: String)

class UserService(database: Database) {
    object DBUser : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val passcode = varchar("passcode", length = 50)
        val wins = integer("wins").default(0)
        val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(DBUser)
        }
    }

    suspend fun create(user: Login): Int = dbQuery {
        DBUser.insert { dbUser ->
            dbUser[name] = user.username
            dbUser[passcode] = user.passcode
        }[DBUser.id]
    }

    suspend fun findById(id: Int): User? {
        return dbQuery {
            DBUser.select { DBUser.id eq id }
                .map { dbUser ->
                    User(
                        dbUser[DBUser.name],
                        dbUser[DBUser.passcode],
                        dbUser[DBUser.wins],
                        dbUser[DBUser.id],
                        dbUser[DBUser.updatedAt].toString()
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun findByUsername(username: String): User? {
        return dbQuery {
            DBUser.select { DBUser.name eq username }
                .map { dbUser ->
                    User(
                        dbUser[DBUser.name],
                        dbUser[DBUser.passcode],
                        dbUser[DBUser.wins],
                        dbUser[DBUser.id],
                        dbUser[DBUser.updatedAt].toString()
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: User) {
        dbQuery {
            DBUser.update({ DBUser.id eq id }) { dbUser ->
                dbUser[name] = user.name
                dbUser[wins] = user.wins
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            DBUser.deleteWhere { DBUser.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

