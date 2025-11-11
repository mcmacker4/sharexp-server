package es.hgg.sharexp.server.api.auth

import es.hgg.sharexp.model.EntityIdResponse
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.respondEither
import io.ktor.server.routing.Route
import io.ktor.server.routing.get


fun Route.status() = get("/status") {
    respondEither {
        val principal = call.getUserPrincipal()
        EntityIdResponse(principal.userId)
    }
}
