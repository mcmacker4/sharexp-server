package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.model.GroupInfo
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.insertGroup
import es.hgg.sharexp.server.persistence.repositories.insertGroupMember
import es.hgg.sharexp.server.persistence.repositories.selectGroupById
import es.hgg.sharexp.server.persistence.repositories.selectUserDisplayName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

private val logger: Logger = LoggerFactory.getLogger("GroupService")

suspend fun Raise<AppError>.createGroup(groupName: String, principal: UserPrincipal): Uuid {
    val ownerId = principal.userId

    val ownerDisplayName = ensureNotNull(selectUserDisplayName(ownerId)) {
        AppError.Internal.also { logger.error("Could not select user's display name") }
    }

    val groupId = ensureNotNull(insertGroup(groupName, principal.userId)) {
        AppError.Internal.also { logger.error("Could not insert new group") }
    }

    ensureNotNull(insertGroupMember(groupId, ownerDisplayName, ownerId)) {
        AppError.Internal.also { logger.error("Could not insert owner as group member of new group") }
    }

    return groupId
}

suspend fun Raise<AppError>.fetchGroupData(groupId: Uuid, principal: UserPrincipal): GroupInfo =
    ensureNotNull(selectGroupById(groupId, principal)) { AppError.NotFound }

suspend fun Raise<AppError>.isUserOwnerOfGroup(groupId: Uuid, principal: UserPrincipal): Boolean =
    fetchGroupData(groupId, principal).owner.also { logger.trace("Owner of group {} is {}", groupId, it) } == principal.userId