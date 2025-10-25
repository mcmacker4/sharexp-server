package es.hgg.sharexp.server.api.groups.members

import es.hgg.sharexp.api.model.AddMemberRequest
import es.hgg.sharexp.api.model.NewEntityResponse
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.service.addMemberToGroup
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.postMember() = post {
    val principal = call.getUserPrincipal()

    respondEither {
        val input = call.receive<AddMemberRequest>()
        val id = suspendTransaction { addMemberToGroup(getGroupIdParam(), input, principal) }
        NewEntityResponse(id)
    }
}