package es.hgg.sharexp.server.api.auth

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*

fun Route.authentication() = route("/auth") {
    register()
    login()
    authenticate { status() }
}