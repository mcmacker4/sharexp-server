package es.hgg.sharexp.server.api.groups.members

import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUUID
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.service.fetchGroupMembers
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.listMembers() = get {
    respondEither {
        val groupId = parseUUID(call.pathParameters["groupId"]) { AppError.BadRequest }

        val principal = call.principal<UserPrincipal>()
            ?: throw Exception("User must be authenticated")

        ensureNotNull(suspendTransaction { fetchGroupMembers(groupId, principal) }) { AppError.NotFound }
    }
}