package es.hgg.sharexp.api

import es.hgg.sharexp.api.auth.authentication
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.*

fun Application.configureApi() = routing {
    route("/auth") {
        authentication()
    }
}

val Route.dependencies get() = application.dependencies