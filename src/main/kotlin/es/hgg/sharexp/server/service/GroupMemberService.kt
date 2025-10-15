package es.hgg.sharexp.server.service

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import es.hgg.sharexp.api.model.AddMemberRequest
import es.hgg.sharexp.api.model.MemberInfo
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.insertGroupMember
import es.hgg.sharexp.server.persistence.repositories.selectGroupMembers
import java.util.*


suspend fun Raise<AppError>.fetchGroupMembers(groupId: UUID, principal: UserPrincipal): NonEmptyList<MemberInfo> =
    ensureNotNull(selectGroupMembers(groupId, principal.userId).toNonEmptyListOrNull()) { AppError.NotFound }

suspend fun Raise<AppError>.addMemberToGroup(groupId: UUID, member: AddMemberRequest, principal: UserPrincipal): UUID {
    ensure(isUserOwnerOfGroup(groupId, principal)) { AppError.Forbidden }
    return ensureNotNull(insertGroupMember(groupId, member.name)) { AppError.Conflict }
}