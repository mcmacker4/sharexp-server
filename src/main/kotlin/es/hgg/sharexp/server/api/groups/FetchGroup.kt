package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUUID
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.service.fetchGroupData
import io.ktor.server.auth.*
import io.ktor.server.routing.*


fun Route.fetchGroup() = get {
    respondEither {
        val groupId = parseUUID(call.pathParameters["groupId"]) { AppError.BadRequest }

        val principal = call.principal<UserPrincipal>()
            ?: throw Exception("User must be authenticated")

        fetchGroupData(groupId, principal)
    }
}