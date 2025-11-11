package es.hgg.sharexp.server.service

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import es.hgg.sharexp.model.AddMemberRequest
import es.hgg.sharexp.model.MemberInfo
import es.hgg.sharexp.model.UpdateMemberRequest
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.*
import kotlin.uuid.Uuid


suspend fun Raise<AppError>.fetchGroupMembers(groupId: Uuid, principal: UserPrincipal): NonEmptyList<MemberInfo> =
    ensureNotNull(selectGroupMembers(groupId, principal.userId).toNonEmptyListOrNull()) { AppError.NotFound }

suspend fun Raise<AppError>.addMemberToGroup(groupId: Uuid, member: AddMemberRequest, principal: UserPrincipal): Uuid {
    ensureUserIsGroupOwner(groupId, principal)
    return ensureNotNull(insertGroupMember(groupId, member.name)) { AppError.Conflict }
}

suspend fun Raise<AppError>.modifyMember(groupId: Uuid, memberId: Uuid, data: UpdateMemberRequest, principal: UserPrincipal) {
    ensureUserIsGroupOwner(groupId, principal)
    ensure(updateMember(groupId, memberId, data)) { AppError.Conflict }
}

suspend fun Raise<AppError>.removeMember(groupId: Uuid, memberId: Uuid, principal: UserPrincipal) {
    ensureUserIsGroupOwner(groupId, principal)
    ensure(deleteFromMembers(groupId, memberId)) { AppError.NotFound }
}

suspend fun<E> Raise<E>.ensureUserIsGroupMember(groupId: Uuid, principal: UserPrincipal, raise: () -> E) =
    ensure(isUserMemberOfGroup(principal.userId, groupId), raise)

suspend fun Raise<AppError>.ensureUserIsGroupOwner(groupId: Uuid, principal: UserPrincipal) {
    ensure(isUserOwnerOfGroup(groupId, principal)) { AppError.Forbidden }
}