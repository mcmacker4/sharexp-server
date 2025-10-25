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
import es.hgg.sharexp.server.persistence.repositories.deleteFromMembers
import es.hgg.sharexp.server.persistence.repositories.insertGroupMember
import es.hgg.sharexp.server.persistence.repositories.isUserMemberOfGroup
import es.hgg.sharexp.server.persistence.repositories.selectGroupMembers
import es.hgg.sharexp.server.persistence.repositories.updateMember
import java.util.*


suspend fun Raise<AppError>.fetchGroupMembers(groupId: UUID, principal: UserPrincipal): NonEmptyList<MemberInfo> =
    ensureNotNull(selectGroupMembers(groupId, principal.userId).toNonEmptyListOrNull()) { AppError.NotFound }

suspend fun Raise<AppError>.addMemberToGroup(groupId: UUID, member: AddMemberRequest, principal: UserPrincipal): UUID {
    ensureUserIsGroupOwner(groupId, principal)
    return ensureNotNull(insertGroupMember(groupId, member.name)) { AppError.Conflict }
}

suspend fun Raise<AppError>.modifyMember(groupId: UUID, memberId: UUID, data: UpdateMemberRequest, principal: UserPrincipal) {
    ensureUserIsGroupOwner(groupId, principal)
    ensure(updateMember(groupId, memberId, data)) { AppError.Conflict }
}

suspend fun Raise<AppError>.removeMember(groupId: UUID, memberId: UUID, principal: UserPrincipal) {
    ensureUserIsGroupOwner(groupId, principal)
    ensure(deleteFromMembers(groupId, memberId)) { AppError.NotFound }
}

suspend fun<E> Raise<E>.ensureUserIsGroupMember(groupId: UUID, principal: UserPrincipal, raise: () -> E) =
    ensure(isUserMemberOfGroup(principal.userId, groupId), raise)

suspend fun Raise<AppError>.ensureUserIsGroupOwner(groupId: UUID, principal: UserPrincipal) {
    ensure(isUserOwnerOfGroup(groupId, principal)) { AppError.Forbidden }
}