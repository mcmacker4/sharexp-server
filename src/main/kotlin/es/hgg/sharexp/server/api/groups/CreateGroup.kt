package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.api.model.CreateGroupRequest
import es.hgg.sharexp.api.model.CreateGroupResponse
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.service.createGroup
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.createGroup() = post {
    val principal = call.principal<UserPrincipal>()
        ?: throw Exception("User must be authenticated")

    val input = call.receive<CreateGroupRequest>()

    val groupId = createGroup(input.name, principal)

    call.respond(CreateGroupResponse(groupId))
}