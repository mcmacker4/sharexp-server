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
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Join
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.exists
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

suspend fun selectGroupById(groupId: UUID, principal: UserPrincipal): GroupInfoWithMembers? =
    withContext(Dispatchers.IO) wc@{
        val groupInfo = joinGroupsAndMembers { GroupMembers.user eq principal.userId }
            .select(Groups.id, Groups.name, GroupMembers.id)
            .where { isVisibleByUser(principal.userId) and (Groups.id eq groupId) }
            .map { it.intoGroupInfo() }
            .singleOrNull() ?: return@wc null

        val members = GroupMembers.selectAll().where {
            GroupMembers.groupId eq groupId
        }.map { it.intoMemberInfo() }.toList()

        GroupInfoWithMembers(groupInfo, members)
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

private fun isVisibleByUser(userId: UUID): Expression<Boolean> =
    exists(GroupMembers.selectAll().where { GroupMembers.user eq userId })

private fun ResultRow.intoGroupInfo(): GroupInfo = GroupInfo(this[Groups.id], this[Groups.name], this[GroupMembers.id])
private fun ResultRow.intoMemberInfo(): MemberInfo = MemberInfo(this[GroupMembers.id], this[GroupMembers.name])