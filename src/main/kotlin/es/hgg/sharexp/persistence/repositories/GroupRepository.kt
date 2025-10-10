package es.hgg.sharexp.persistence.repositories

import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.data.GroupInfo
import es.hgg.sharexp.data.GroupInfoWithMembers
import es.hgg.sharexp.data.MemberInfo
import es.hgg.sharexp.persistence.tables.GroupMembers
import es.hgg.sharexp.persistence.tables.Groups
import es.hgg.sharexp.persistence.tables.insertReturningId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import java.util.*

suspend fun insertGroup(name: String, owner: UUID): UUID? = withContext(Dispatchers.IO) {
    Groups.insertReturningId(Groups.id) {
        it[Groups.name] = name
        it[Groups.owner] = owner
    }
}

suspend fun insertGroupMember(groupId: UUID, name: String, userId: UUID? = null): UUID? = withContext(Dispatchers.IO) {
    GroupMembers.insertReturningId(GroupMembers.id) {
        it[GroupMembers.groupId] = groupId
        it[GroupMembers.name] = name
        it[GroupMembers.user] = userId
    }
}

suspend fun selectGroupById(groupId: UUID, principal: UserPrincipal): GroupInfoWithMembers? {
    return withContext(Dispatchers.IO) wc@{
        // Could we run these two queries concurrently or is it overkill?
        val groupInfo = selectFromVisibleGroups(principal.userId) { Groups.id eq groupId }
            .map { it.intoGroupInfo() }
            .singleOrNull() ?: return@wc null

        val members = GroupMembers.select(GroupMembers.id, GroupMembers.name).where {
            GroupMembers.groupId eq groupId
        }.map { it.intoMemberInfo() }.toList()

        GroupInfoWithMembers(groupInfo, members)
    }
}

suspend fun selectAllVisibleGroups(principal: UserPrincipal): List<GroupInfo> =
    selectFromVisibleGroups(principal.userId)
        .map { it.intoGroupInfo() }
        .toList()

private fun selectFromVisibleGroups(
    userId: UUID,
    additionalConstraint: (SqlExpressionBuilder.() -> Op<Boolean>)? = null
): Query {
    return joinGroupsAndMembers { GroupMembers.user eq userId }
        .select(Groups.id, Groups.name, GroupMembers.id)
        .where {
            val visible = isVisibleByUser(userId)
            val additional = additionalConstraint?.invoke(this@where)
            if (additional != null) (visible and additional) else visible
        }
}

private fun joinGroupsAndMembers(
    joinType: JoinType = JoinType.INNER,
    additionalConstraint: (SqlExpressionBuilder.() -> Op<Boolean>)? = null
): Join {
    return Groups.join(
        GroupMembers,
        onColumn = Groups.id,
        otherColumn = GroupMembers.groupId,
        joinType = joinType,
        additionalConstraint = additionalConstraint
    )
}

private fun isVisibleByUser(userId: UUID): Op<Boolean> =
    exists(GroupMembers.selectAll().where { GroupMembers.user eq userId })

private fun ResultRow.intoGroupInfo(): GroupInfo = GroupInfo(this[Groups.id], this[Groups.name], this[GroupMembers.id])
private fun ResultRow.intoMemberInfo(): MemberInfo = MemberInfo(this[GroupMembers.id], this[GroupMembers.name])