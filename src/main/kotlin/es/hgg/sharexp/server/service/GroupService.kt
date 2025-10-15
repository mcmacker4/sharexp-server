package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.api.model.GroupInfo
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.insertGroup
import es.hgg.sharexp.server.persistence.repositories.insertGroupMember
import es.hgg.sharexp.server.persistence.repositories.selectGroupById
import es.hgg.sharexp.server.persistence.repositories.selectUserDisplayName
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

suspend fun createGroup(groupName: String, principal: UserPrincipal): UUID = suspendTransaction {
    val ownerId = principal.userId

    val ownerDisplayName = selectUserDisplayName(ownerId)
        ?: throw Exception("Could not retrieve user's display name")

    val groupId = insertGroup(groupName, principal.userId)
        ?: throw Exception("Could not create group")

    insertGroupMember(groupId, ownerDisplayName, ownerId)
        ?: throw Exception("Could not add owner as group member")

    groupId
}

suspend fun Raise<AppError>.fetchGroupData(groupId: UUID, principal: UserPrincipal): GroupInfo =
    ensureNotNull(selectGroupById(groupId, principal).also { println(it) }) { AppError.NotFound }

suspend fun Raise<AppError>.isUserOwnerOfGroup(groupId: UUID, principal: UserPrincipal): Boolean =
    fetchGroupData(groupId, principal).owner.also { println(it) } == principal.userId