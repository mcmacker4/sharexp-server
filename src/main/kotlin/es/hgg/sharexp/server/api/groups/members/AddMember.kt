package es.hgg.sharexp.server.api.groups.members

import es.hgg.sharexp.api.model.AddMemberRequest
import es.hgg.sharexp.api.model.AddMemberResponse
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUUID
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.service.addMemberToGroup
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.addMember() = post {
    val principal = call.principal<UserPrincipal>()
        ?: throw Exception("User must be authenticated")

    respondEither {
        val groupId = parseUUID(call.pathParameters["groupId"]) { AppError.BadRequest }
        val input = call.receive<AddMemberRequest>()

        val id = suspendTransaction { addMemberToGroup(groupId, input, principal) }

        AddMemberResponse(id)
    }
}