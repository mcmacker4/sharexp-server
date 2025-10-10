package es.hgg.sharexp.persistence.repositories

import es.hgg.sharexp.data.MemberInfo
import es.hgg.sharexp.persistence.tables.GroupMembers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.r2dbc.select
import java.util.*


suspend fun selectGroupMembers(groupId: UUID, userId: UUID): List<MemberInfo> {
    val m1 = GroupMembers.alias("m1")
    val m2 = GroupMembers.alias("m2")

    return m1.join(m2, onColumn = m1[GroupMembers.groupId], otherColumn = m2[GroupMembers.groupId], additionalConstraint = { m2[GroupMembers.user] eq userId }, joinType = JoinType.INNER)
        .select(m1[GroupMembers.id], m1[GroupMembers.name])
        .where { m1[GroupMembers.groupId] eq groupId }
        .map { it.intoMemberInfo(m1) }
        .toList()
}

private fun ResultRow.intoMemberInfo(alias: Alias<GroupMembers>) = MemberInfo(this[alias[GroupMembers.id]], this[alias[GroupMembers.name]])