package es.hgg.sharexp.service

import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.data.MemberInfo
import es.hgg.sharexp.persistence.repositories.selectGroupMembers
import java.util.*


suspend fun fetchGroupMembers(groupId: UUID, principal: UserPrincipal): List<MemberInfo>? {
    return selectGroupMembers(groupId, principal.userId)
        .takeIf { it.isNotEmpty() }
}