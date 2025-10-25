package es.hgg.sharexp.server.persistence.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object Groups : Table("groups") {
    val id = uuid("group_id").autoGenerateV7()

    val name = varchar("name", 50)
    val owner = reference("owner", Users.id, onDelete = ReferenceOption.RESTRICT, onUpdate = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}