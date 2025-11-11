package es.hgg.sharexp.server.persistence.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object Groups : Table("groups") {
    val id = uuid("group_id").autoGenerateV7()

    val name = varchar("name", 50)
    val owner = reference("owner", Users.id, onDelete = ReferenceOption.RESTRICT, onUpdate = ReferenceOption.CASCADE)

    val createdAt = timestamp("created_at")
    // TODO: Use a view instead of a physical column
    val lastActivityAt = timestamp("last_activity_at")

    override val primaryKey = PrimaryKey(id)
}