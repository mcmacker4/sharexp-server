package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.api.model.MemberInfo
import es.hgg.sharexp.api.model.UpdateMemberRequest
import es.hgg.sharexp.server.persistence.tables.GroupMembers
import es.hgg.sharexp.server.persistence.tables.insertReturningId
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.Uuid


class GroupMemberRepository {

    suspend fun selectGroupMembers(groupId: Uuid, userId: Uuid): List<MemberInfo> {
        val m1 = GroupMembers.alias("m1")
        val m2 = GroupMembers.alias("m2")

        return m1.join(
            otherTable = m2,
            joinType = JoinType.INNER,
            onColumn = m1[GroupMembers.groupId],
            otherColumn = m2[GroupMembers.groupId],
            additionalConstraint = { m2[GroupMembers.user] eq userId }
        )
            .select(m1[GroupMembers.id], m1[GroupMembers.name])
            .where { m1[GroupMembers.groupId] eq groupId }
            .map { it.intoMemberInfo(m1) }
            .toList()
    }

    private fun ResultRow.intoMemberInfo(alias: Alias<GroupMembers>) =
        MemberInfo(this[alias[GroupMembers.id]], this[alias[GroupMembers.name]])

    suspend fun insertGroupMember(groupId: Uuid, name: String, userId: Uuid? = null): Uuid? {
        return GroupMembers.insertReturningId(GroupMembers.id, ignoreErrors = true) {
            it[GroupMembers.groupId] = groupId
            it[GroupMembers.name] = name
            // Bug report: https://youtrack.jetbrains.com/issue/EXPOSED-992/Explicitly-inserting-null-in-a-nullable-kotlin.uuid.Uuid-column-fails
            // Pull request: https://github.com/JetBrains/Exposed/pull/2750
            if (userId != null) it[user] = userId
        }
    }

    suspend fun deleteFromMembers(groupId: Uuid, memberId: Uuid): Boolean {
        return GroupMembers.deleteWhere {
            (GroupMembers.groupId eq groupId) and (GroupMembers.id eq memberId)
        } > 0
    }

    suspend fun updateMember(groupId: Uuid, memberId: Uuid, data: UpdateMemberRequest): Boolean {
        return GroupMembers.update(
            where = { (GroupMembers.groupId eq groupId) and (GroupMembers.id eq memberId) },
            body = { it[GroupMembers.name] = data.name }
        ) > 0
    }

    suspend fun isUserMemberOfGroup(userId: Uuid, groupId: Uuid): Boolean {
        return GroupMembers.selectAll()
            .where { (GroupMembers.groupId eq groupId) and (GroupMembers.user eq userId) }
            .count() > 0
    }

}