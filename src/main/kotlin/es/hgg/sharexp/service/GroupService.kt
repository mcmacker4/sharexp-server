package es.hgg.sharexp.service

import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.data.GroupInfoWithMembers
import es.hgg.sharexp.persistence.repositories.insertGroup
import es.hgg.sharexp.persistence.repositories.insertGroupMember
import es.hgg.sharexp.persistence.repositories.selectGroupById
import es.hgg.sharexp.persistence.repositories.selectUserDisplayName
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

suspend fun createGroup(groupName: String, principal: UserPrincipal): UUID = suspendTransaction {
    val ownerId = principal.userId

    val ownerDisplayName = selectUserDisplayName(ownerId)
        ?: throw Exception("Could not retrieve user's display name")

    val groupId = insertGroup(groupName, principal.userId)
        ?: throw Exception("Could not create group")

    addMember(groupId, ownerDisplayName, ownerId)
        ?: throw Exception("Could not add owner as group member")

    groupId
}

suspend fun addMember(groupId: UUID, name: String, userId: UUID? = null): UUID? =
    insertGroupMember(groupId, name, userId)

suspend fun fetchGroupData(groupId: UUID, principal: UserPrincipal): GroupInfoWithMembers? {
    return selectGroupById(groupId, principal)
}