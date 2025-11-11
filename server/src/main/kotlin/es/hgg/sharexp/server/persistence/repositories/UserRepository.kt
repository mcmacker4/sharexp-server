package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.server.persistence.tables.Users
import es.hgg.sharexp.server.persistence.tables.insertReturningId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.select
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

suspend fun selectUserIdAndHashByEmail(email: String): Pair<Uuid, String>? = withContext(Dispatchers.IO) {
    Users.select(Users.id, Users.pwHash)
        .where { Users.email eq email }
        .map { it[Users.id].toKotlinUuid() to it[Users.pwHash].decodeToString() }
        .singleOrNull()
}

suspend fun insertUser(username: String, email: String, pwHash: String): UUID? = withContext(Dispatchers.IO) {
    Users.insertReturningId(Users.id, ignoreErrors = true) {
        it[Users.username] = username
        it[Users.email] = email
        it[Users.pwHash] = pwHash.toByteArray()
    }
}

suspend fun selectUserDisplayName(userId: Uuid): String? = withContext(Dispatchers.IO) {
    Users.select(Users.username)
        .where { Users.id eq userId.toJavaUuid() }
        .singleOrNull()
        ?.get(Users.username)
}
