package es.hgg.sharexp.server.persistence.tables

import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.Uuid

object Users : Table("users") {
    val id = uuid("user_id").clientDefault { Uuid.generateV7() }

    val username = varchar("user_name", 32).uniqueIndex()
    val email = varchar("email", 128).uniqueIndex()
    val pwHash = binary("pw_hash", 60)

    override val primaryKey = PrimaryKey(id)
}