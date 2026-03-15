package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import es.hgg.sharexp.api.model.GroupInfo
import es.hgg.sharexp.api.model.GroupSort
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.GroupMemberRepository
import es.hgg.sharexp.server.persistence.repositories.GroupRepository
import es.hgg.sharexp.server.persistence.repositories.UserRepository
import es.hgg.sharexp.server.util.PageRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

class GroupService(
    val groupRepo: GroupRepository,
    val memberRepo: GroupMemberRepository,
    val userRepo: UserRepository,
) {

    private val logger: Logger = LoggerFactory.getLogger("GroupService")

    context(_: Raise<AppError>)
    suspend fun createGroup(groupName: String, principal: UserPrincipal): Uuid {
        val ownerId = principal.userId

        val ownerDisplayName = ensureNotNull(userRepo.selectUserDisplayName(ownerId)) {
            AppError.Internal.also { logger.error("Could not select user's display name") }
        }

        val groupId = ensureNotNull(groupRepo.insertGroup(groupName, principal.userId)) {
            AppError.Internal.also { logger.error("Could not insert new group") }
        }

        ensureNotNull(memberRepo.insertGroupMember(groupId, ownerDisplayName, ownerId)) {
            AppError.Internal.also { logger.error("Could not insert owner as group member of new group") }
        }

        return groupId
    }

    suspend fun fetchAllVisibleGroups(
        pageRequest: PageRequest<GroupSort>,
        principal: UserPrincipal
    ): List<GroupInfo> = groupRepo.selectAllVisibleGroups(pageRequest, principal)

    context(_: Raise<AppError>)
    suspend fun fetchGroupData(groupId: Uuid, principal: UserPrincipal): GroupInfo {
        return ensureNotNull(groupRepo.selectGroupById(groupId, principal)) { AppError.NotFound }
    }

    context(_: Raise<AppError>)
    suspend fun isUserOwnerOfGroup(groupId: Uuid, principal: UserPrincipal): Boolean {
        val owner = fetchGroupData(groupId, principal).owner
        logger.trace("Owner of group {} is {}", groupId, owner)
        return owner == principal.userId
    }

}