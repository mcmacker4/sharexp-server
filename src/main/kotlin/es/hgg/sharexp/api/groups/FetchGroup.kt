package es.hgg.sharexp.api.groups

import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.api.parseUUID
import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.service.fetchGroupData
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


sealed interface FetchGroupError {
    object BadRequest : FetchGroupError
    object NotFound : FetchGroupError
}

fun Route.fetchGroup() = get {
    either {
        val groupId = parseUUID(call.pathParameters["groupId"]) { FetchGroupError.BadRequest }

        val principal = call.principal<UserPrincipal>()
            ?: throw Exception("User must be authenticated")

        ensureNotNull(fetchGroupData(groupId, principal)) { FetchGroupError.NotFound }
    }.fold({
        error -> call.respond(when (error) {
            is FetchGroupError.BadRequest -> HttpStatusCode.BadRequest
            is FetchGroupError.NotFound -> HttpStatusCode.NotFound
        })
    }, {
        info -> call.respond(info)
    })
}