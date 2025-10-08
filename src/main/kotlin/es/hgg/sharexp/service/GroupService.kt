package es.hgg.sharexp.service

import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.data.GroupInfoWithMembers
import es.hgg.sharexp.persistence.repositories.GroupRepository
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

class GroupService(val repo: GroupRepository, val userService: UserService) {

    suspend fun createGroup(groupName: String, principal: UserPrincipal): UUID = suspendTransaction {
        val ownerId = principal.userId

        val ownerDisplayName = userService.getUserDisplayName(ownerId)
            ?: throw Exception("Could not retrieve user's display name")

        val groupId = repo.createGroup(groupName, principal.userId)
            ?: throw Exception("Could not create group")

        addMember(groupId, ownerDisplayName, ownerId)
            ?: throw Exception("Could not add owner as group member")

        groupId
    }

    suspend fun addMember(groupId: UUID, name: String, userId: UUID? = null): UUID? =
        repo.addGroupMember(groupId, name, userId)

    suspend fun fetchGroupData(groupId: UUID, principal: UserPrincipal): GroupInfoWithMembers? {
        return repo.fetchGroupById(groupId, principal)
    }

}