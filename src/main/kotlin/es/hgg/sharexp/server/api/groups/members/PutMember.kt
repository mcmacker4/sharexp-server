package es.hgg.sharexp.server.api.groups.members

import es.hgg.sharexp.api.model.UpdateMemberRequest
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondIfError
import es.hgg.sharexp.server.service.GroupMemberService
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.putMember() = put {
    val service by inject<GroupMemberService>()
    respondIfError {
        val data = call.receive<UpdateMemberRequest>()
        service.modifyMember(call.getGroupIdParam(), call.getMemberIdParam(), data, call.getUserPrincipal())
    }
}