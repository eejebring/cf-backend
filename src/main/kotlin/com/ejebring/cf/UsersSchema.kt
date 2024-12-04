package com.ejebring.cf

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

data class User(val name: String, val passcode: String, val wins: Int, val updatedAt: LocalDateTime)

object DBUser : Table() {
    val name = varchar("name", length = 50)
    val passcode = varchar("passcode", length = 50)
    val wins = integer("wins").default(0)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(name)
}

class UserService(database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(DBUser)
        }
    }

    suspend fun create(user: Login): String = dbQuery {
        DBUser.insert { dbUser ->
            dbUser[name] = user.username
            dbUser[passcode] = user.passcode
        }[DBUser.name]
    }

    suspend fun findByUsername(username: String): User? {
        return dbQuery {
            DBUser.selectAll()
                .where { DBUser.name eq username }
                .map { dbUser ->
                    User(
                        dbUser[DBUser.name],
                        dbUser[DBUser.passcode],
                        dbUser[DBUser.wins],
                        dbUser[DBUser.updatedAt]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun allUsers(): List<User> = dbQuery {
        DBUser.selectAll()
            .map { dbUser ->
                User(
                    dbUser[DBUser.name],
                    dbUser[DBUser.passcode],
                    dbUser[DBUser.wins],
                    dbUser[DBUser.updatedAt]
                )
            }
    }

    suspend fun winIncrement(name: String) {
        dbQuery {
            DBUser.update({ DBUser.name eq name }) { dbUser ->
            }
        }
    }

    suspend fun delete(name: String) {
        dbQuery {
            DBUser.deleteWhere { DBUser.name.eq(name) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

