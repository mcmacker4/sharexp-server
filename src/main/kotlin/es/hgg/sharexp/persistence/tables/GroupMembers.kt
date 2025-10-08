package es.hgg.sharexp.persistence.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object GroupMembers : Table("group_members") {
    val groupId = reference("group_id", Groups.id, onDelete = ReferenceOption.CASCADE)
    val id = uuid("member_id").autoGenerate()

    val name = varchar("name", 50)
    val user = reference("user", Users.id).nullable()

    override val primaryKey = PrimaryKey(groupId, id)
}