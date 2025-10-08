package es.hgg.sharexp.api.groups

import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.api.dependencies
import es.hgg.sharexp.api.parseUUID
import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.service.GroupService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


sealed interface FetchGroupError {
    object BadRequest : FetchGroupError
    object NotFound : FetchGroupError
}

fun Route.fetchGroup() = get {
    val service: GroupService by dependencies

    either {
        val groupId = parseUUID(call.pathParameters["groupId"]) { FetchGroupError.BadRequest }

        val principal = call.principal<UserPrincipal>()
            ?: throw Exception("User must be authenticated")

        ensureNotNull(service.fetchGroupData(groupId, principal)) { FetchGroupError.NotFound }
    }.fold({
        error -> call.respond(when (error) {
            is FetchGroupError.BadRequest -> HttpStatusCode.BadRequest
            is FetchGroupError.NotFound -> HttpStatusCode.NotFound
        })
    }, {
        info -> call.respond(info)
    })
}