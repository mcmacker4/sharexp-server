package es.hgg.sharexp.server.persistence.tables

import org.jetbrains.exposed.v1.core.Table

object Users : Table("users") {
    val id = uuid("user_id").autoGenerate()

    val username = varchar("user_name", 32).uniqueIndex()
    val email = varchar("email", 128).uniqueIndex()
    val pwHash = binary("pw_hash", 60)

    override val primaryKey = PrimaryKey(id)
}