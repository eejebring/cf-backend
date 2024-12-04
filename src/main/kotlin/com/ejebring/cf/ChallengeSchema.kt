package com.ejebring.cf

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DBChallenge : Table() {
    val challenger = varchar("challenger", length = 50) references DBUser.name
    val challenged = varchar("challenged", length = 50) references DBUser.name

    override val primaryKey = PrimaryKey(challenger, challenged)
}

data class Challenge(val challenger: String, val challenged: String)

class ChallengeSchema(database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(DBChallenge)
        }
    }

    suspend fun create(challenger: String, challenged: String) {
        dbQuery {
            DBChallenge.insert {
                it[DBChallenge.challenger] = challenger
                it[DBChallenge.challenged] = challenged
            }
        }
    }

    suspend fun getUserChallenges(username: String): List<Challenge> {
        return dbQuery {
            DBChallenge.selectAll()
                .where { (DBChallenge.challenger eq username) or (DBChallenge.challenged eq username) }
                .map { Challenge(it[DBChallenge.challenger], it[DBChallenge.challenged]) }
        }
    }

    suspend fun remove(challenge: Challenge) {

        dbQuery {
            // Do not know why the eq operator is not working in the deleteWhere function
            //TODO: Fix the eq operator so that the delete function can be more generic
            val conn = TransactionManager.current()
                .exec("delete from DBChallenge where challenger = '${challenge.challenged}' and challenged = '${challenge.challenger}'")

            /*DBChallenge.deleteWhere {
                (DBChallenge.challenger.eq(challenge.challenger) and (DBChallenge.challenged eq challenge.challenged)
            }*/
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}