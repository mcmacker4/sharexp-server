package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.api.model.GroupInfo
import es.hgg.sharexp.api.model.GroupSort
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.tables.GroupMembers
import es.hgg.sharexp.server.persistence.tables.Groups
import es.hgg.sharexp.server.persistence.tables.insertReturningId
import es.hgg.sharexp.server.persistence.tables.page
import es.hgg.sharexp.server.util.PageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend fun insertGroup(name: String, owner: UUID): UUID? = withContext(Dispatchers.IO) {
    val now = Clock.System.now()

    Groups.insertReturningId(Groups.id) {
        it[Groups.name] = name
        it[Groups.owner] = owner
        it[Groups.createdAt] = now
        it[Groups.lastActivityAt] = now
    }
}

suspend fun selectGroupById(groupId: UUID, principal: UserPrincipal): GroupInfo? {
    return withContext(Dispatchers.IO) {
        joinGroupsAndMembers { GroupMembers.user eq principal.userId }
            .select(Groups.id, Groups.name, Groups.owner, GroupMembers.id)
            .where { (Groups.id eq groupId) }
            .map { it.intoGroupInfo() }
            .singleOrNull()
    }
}

suspend fun selectAllVisibleGroups(pageRequest: PageRequest<GroupSort>, principal: UserPrincipal): List<GroupInfo> {
    return withContext(Dispatchers.IO) {
        joinGroupsAndMembers { GroupMembers.user eq principal.userId }
            .select(Groups.id, Groups.name, Groups.owner, GroupMembers.id)
            .page(pageRequest) { it.toSortColumn() }
            .map { it.intoGroupInfo() }
            .toList()
    }
}

@OptIn(ExperimentalTime::class)
private fun GroupSort.toSortColumn(): Expression<*> = when (this) {
    GroupSort.CREATED -> Groups.createdAt
    GroupSort.MODIFIED -> Groups.lastActivityAt
}

@OptIn(ExperimentalTime::class)
suspend fun updateGroupActivityTimestamp(groupId: UUID) = withContext(Dispatchers.IO) {
    Groups.update(where = { Groups.id eq groupId }, limit = 1) {
        it[Groups.lastActivityAt] = Clock.System.now()
    }
}

private fun joinGroupsAndMembers(
    joinType: JoinType = JoinType.INNER,
    additionalConstraint: (() -> Op<Boolean>)? = null
): Join {
    return Groups.join(
        GroupMembers,
        onColumn = Groups.id,
        otherColumn = GroupMembers.groupId,
        joinType = joinType,
        additionalConstraint = additionalConstraint
    )
}

private fun ResultRow.intoGroupInfo(): GroupInfo = GroupInfo(this[Groups.id], this[Groups.name], this[Groups.owner], this[GroupMembers.id])