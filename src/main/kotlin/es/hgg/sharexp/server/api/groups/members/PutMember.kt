package es.hgg.sharexp.server.api.groups.members

import es.hgg.sharexp.api.model.UpdateMemberRequest
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondIfError
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.service.modifyMember
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.putMember() = put {
    respondIfError {
        val data = call.receive<UpdateMemberRequest>()
        modifyMember(getGroupIdParam(), getMemberIdParam(), data, call.getUserPrincipal())
    }
}