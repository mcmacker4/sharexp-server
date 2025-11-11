package es.hgg.sharexp.server.app.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlin.time.Duration.Companion.days

fun Application.configureSessions() {
    install(Sessions) {
        cookie<UserPrincipal>("sxp_session", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "Strict"
            cookie.maxAge = 3.days
        }
    }
}