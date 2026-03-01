package es.hgg.sharexp.server.api.groups.members

import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondIfError
import es.hgg.sharexp.server.service.GroupMemberService
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Route.deleteMember() = delete {
    val service by inject<GroupMemberService>()
    respondIfError {
        service.removeMember(
            groupId = call.getGroupIdParam(),
            memberId = call.getMemberIdParam(),
            principal = call.getUserPrincipal()
        )
    }
}
