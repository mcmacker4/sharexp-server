package es.hgg.sharexp.server.service

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import es.hgg.sharexp.api.model.AddMemberRequest
import es.hgg.sharexp.api.model.MemberInfo
import es.hgg.sharexp.api.model.UpdateMemberRequest
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.GroupMemberRepository
import kotlin.uuid.Uuid


class GroupMemberService(
    val repo: GroupMemberRepository,
    val groupService: GroupService,
) {

    context(raise: Raise<AppError>)
    suspend fun fetchGroupMembers(groupId: Uuid, principal: UserPrincipal): NonEmptyList<MemberInfo> = with(raise) {
        ensureNotNull(repo.selectGroupMembers(groupId, principal.userId).toNonEmptyListOrNull()) { AppError.NotFound }
    }

    context(raise: Raise<AppError>)
    suspend fun addMemberToGroup(
        groupId: Uuid,
        member: AddMemberRequest,
        principal: UserPrincipal
    ): Uuid = with(raise) {
        ensureUserIsGroupOwner(groupId, principal)
        return ensureNotNull(repo.insertGroupMember(groupId, member.name)) { AppError.Conflict }
    }

    context(raise: Raise<AppError>)
    suspend fun modifyMember(
        groupId: Uuid,
        memberId: Uuid,
        data: UpdateMemberRequest,
        principal: UserPrincipal
    ): Unit = with(raise) {
        ensureUserIsGroupOwner(groupId, principal)
        ensure(repo.updateMember(groupId, memberId, data)) { AppError.Conflict }
    }

    context(raise: Raise<AppError>)
    suspend fun removeMember(groupId: Uuid, memberId: Uuid, principal: UserPrincipal): Unit = with(raise) {
        ensureUserIsGroupOwner(groupId, principal)
        ensure(repo.deleteFromMembers(groupId, memberId)) { AppError.NotFound }
    }

    context(raise: Raise<E>)
    suspend fun <E> ensureUserIsGroupMember(groupId: Uuid, principal: UserPrincipal, errorFactory: () -> E) = with(raise) {
        ensure(repo.isUserMemberOfGroup(principal.userId, groupId), errorFactory)
    }

    context(raise: Raise<AppError>)
    suspend fun ensureUserIsGroupOwner(groupId: Uuid, principal: UserPrincipal): Unit = with(raise) {
        ensure(groupService.isUserOwnerOfGroup(groupId, principal)) { AppError.Forbidden }
    }

}