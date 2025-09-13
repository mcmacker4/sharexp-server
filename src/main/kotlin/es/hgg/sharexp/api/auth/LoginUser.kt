package es.hgg.sharexp.api.auth

import es.hgg.sharexp.app.plugins.UserPrincipal
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*


fun Route.login() = authenticate("form") {
    post("/login") {
        val principal = call.principal<UserPrincipal>()!!
        call.sessions.set(principal)
        call.respondRedirect("/")
    }
}