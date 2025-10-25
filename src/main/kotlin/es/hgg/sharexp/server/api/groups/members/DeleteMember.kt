package es.hgg.sharexp.server.api.groups.members

import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondIfError
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.service.removeMember
import io.ktor.server.routing.*


fun Route.deleteMember() = delete {
    respondIfError {
        removeMember(getGroupIdParam(), getMemberIdParam(), call.getUserPrincipal())
    }
}
