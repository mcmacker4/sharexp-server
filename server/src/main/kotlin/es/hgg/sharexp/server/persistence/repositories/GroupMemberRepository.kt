package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.model.MemberInfo
import es.hgg.sharexp.model.UpdateMemberRequest
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
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid


suspend fun selectGroupMembers(groupId: Uuid, userId: Uuid): List<MemberInfo> {
    val m1 = GroupMembers.alias("m1")
    val m2 = GroupMembers.alias("m2")

    val query = m1.join(m2, JoinType.INNER, m1[GroupMembers.groupId], m2[GroupMembers.groupId], additionalConstraint = { m2[GroupMembers.user] eq userId.toJavaUuid() })
        .select(m1[GroupMembers.id], m1[GroupMembers.name])
        .where { m1[GroupMembers.groupId] eq groupId.toJavaUuid() }
        .map { it.intoMemberInfo(m1) }

    return withContext(Dispatchers.IO) { query.toList() }
}

private fun ResultRow.intoMemberInfo(alias: Alias<GroupMembers>) =
    MemberInfo(this[alias[GroupMembers.id]].toKotlinUuid(), this[alias[GroupMembers.name]])

suspend fun insertGroupMember(groupId: Uuid, name: String, userId: Uuid? = null): Uuid? = withContext(Dispatchers.IO) {
    GroupMembers.insertReturningId(GroupMembers.id, ignoreErrors = true) {
        it[GroupMembers.groupId] = groupId.toJavaUuid()
        it[GroupMembers.name] = name
        it[GroupMembers.user] = userId?.toJavaUuid()
    }?.toKotlinUuid()
}

suspend fun deleteFromMembers(groupId: Uuid, memberId: Uuid): Boolean = withContext(Dispatchers.IO) {
    GroupMembers.deleteWhere {
        (GroupMembers.groupId eq groupId.toJavaUuid()) and (GroupMembers.id eq memberId.toJavaUuid())
    } > 0
}

suspend fun updateMember(groupId: Uuid, memberId: Uuid, data: UpdateMemberRequest): Boolean = withContext(Dispatchers.IO) {
    GroupMembers.update({ (GroupMembers.groupId eq groupId.toJavaUuid()) and (GroupMembers.id eq memberId.toJavaUuid()) }, limit = 1) {
        it[GroupMembers.name] = data.name
    } > 0
}

suspend fun isUserMemberOfGroup(userId: Uuid, groupId: Uuid): Boolean = withContext(Dispatchers.IO) {
    GroupMembers.selectAll()
        .where { (GroupMembers.groupId eq groupId.toJavaUuid()) and (GroupMembers.user eq userId.toJavaUuid()) }
        .count() > 0
}