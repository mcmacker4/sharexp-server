package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.api.model.GroupInfo
import es.hgg.sharexp.server.persistence.tables.GroupMembers
import es.hgg.sharexp.server.persistence.tables.Groups
import es.hgg.sharexp.server.persistence.tables.insertReturningId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import java.util.*

suspend fun insertGroup(name: String, owner: UUID): UUID? = withContext(Dispatchers.IO) {
    Groups.insertReturningId(Groups.id) {
        it[Groups.name] = name
        it[Groups.owner] = owner
    }
}

suspend fun selectGroupById(groupId: UUID, principal: UserPrincipal): GroupInfo? {
    return withContext(Dispatchers.IO) {
        joinGroupsAndMembers { GroupMembers.user eq principal.userId }
            .select(Groups.id, Groups.name, Groups.owner, GroupMembers.id)
            .where { (Groups.id eq groupId) and isVisibleByUser(principal.userId) }
            .map { it.intoGroupInfo() }
            .singleOrNull()
    }
}

suspend fun selectAllVisibleGroups(principal: UserPrincipal): List<GroupInfo> {
    return withContext(Dispatchers.IO) {
        joinGroupsAndMembers { GroupMembers.user eq principal.userId }
            .select(Groups.id, Groups.name, Groups.owner, GroupMembers.id)
            .where { isVisibleByUser(principal.userId) }
            .map { it.intoGroupInfo() }
            .toList()
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

private fun ResultRow.intoGroupInfo(): GroupInfo = GroupInfo(this[Groups.id], this[Groups.name], this[Groups.owner], this[GroupMembers.id])