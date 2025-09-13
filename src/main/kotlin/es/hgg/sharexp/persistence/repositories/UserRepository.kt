package es.hgg.sharexp.persistence.repositories

import es.hgg.sharexp.persistence.tables.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

interface UserRepository {
    suspend fun fetchIdAndHashByEmail(email: String): Pair<UUID, String>?
    suspend fun createUser(username: String, email: String, pwHash: String): UUID?
}

class UserRepositoryImpl : UserRepository {

    override suspend fun fetchIdAndHashByEmail(email: String): Pair<UUID, String>? {
        return suspendTransaction(Dispatchers.IO) {
            Users.select(Users.id, Users.pwHash)
                .where { Users.email eq email }
                .map { it[Users.id] to it[Users.pwHash].decodeToString() }
                .singleOrNull()
        }
    }

    override suspend fun createUser(username: String, email: String, pwHash: String): UUID? {
        return suspendTransaction(Dispatchers.IO) {
            Users.insertReturning(listOf(Users.id)) {
                it[Users.username] = username
                it[Users.email] = email
                it[Users.pwHash] = pwHash.toByteArray()
            }.singleOrNull()?.get(Users.id)
        }
    }

}

