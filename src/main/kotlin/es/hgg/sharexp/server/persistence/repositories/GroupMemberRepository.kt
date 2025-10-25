package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.api.model.MemberInfo
import es.hgg.sharexp.api.model.UpdateMemberRequest
import es.hgg.sharexp.server.persistence.tables.GroupMembers
import es.hgg.sharexp.server.persistence.tables.insertReturningId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*


suspend fun selectGroupMembers(groupId: UUID, userId: UUID): List<MemberInfo> {
    val m1 = GroupMembers.alias("m1")
    val m2 = GroupMembers.alias("m2")

    val query = m1.join(m2, JoinType.INNER, m1[GroupMembers.groupId], m2[GroupMembers.groupId], additionalConstraint = { m2[GroupMembers.user] eq userId })
        .select(m1[GroupMembers.id], m1[GroupMembers.name])
        .where { m1[GroupMembers.groupId] eq groupId }
        .map { it.intoMemberInfo(m1) }

    return withContext(Dispatchers.IO) { query.toList() }
}

private fun ResultRow.intoMemberInfo(alias: Alias<GroupMembers>) =
    MemberInfo(this[alias[GroupMembers.id]], this[alias[GroupMembers.name]])

suspend fun insertGroupMember(groupId: UUID, name: String, userId: UUID? = null): UUID? = withContext(Dispatchers.IO) {
    GroupMembers.insertReturningId(GroupMembers.id, ignoreErrors = true) {
        it[GroupMembers.groupId] = groupId
        it[GroupMembers.name] = name
        it[GroupMembers.user] = userId
    }
}

suspend fun deleteFromMembers(groupId: UUID, memberId: UUID): Boolean = withContext(Dispatchers.IO) {
    GroupMembers.deleteWhere {
        (GroupMembers.groupId eq groupId) and (GroupMembers.id eq memberId)
    } > 0
}

suspend fun updateMember(groupId: UUID, memberId: UUID, data: UpdateMemberRequest): Boolean = withContext(Dispatchers.IO) {
    GroupMembers.update({ (GroupMembers.groupId eq groupId) and (GroupMembers.id eq memberId) }, limit = 1) {
        it[GroupMembers.name] = data.name
    } > 0
}

suspend fun isUserMemberOfGroup(userId: UUID, groupId: UUID): Boolean = withContext(Dispatchers.IO) {
    GroupMembers.selectAll()
        .where { (GroupMembers.groupId eq groupId) and (GroupMembers.user eq userId) }
        .count() > 0
}