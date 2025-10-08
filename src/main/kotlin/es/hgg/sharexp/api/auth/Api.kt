package es.hgg.sharexp.api.auth

import io.ktor.server.routing.*

fun Route.authentication() = route("/auth") {
    register()
    login()
}