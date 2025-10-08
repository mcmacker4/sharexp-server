package es.hgg.sharexp.api

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.api.auth.authentication
import es.hgg.sharexp.api.groups.groupsApi
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.*
import java.util.UUID

fun Application.configureApi() = routing {
    authentication()
    authenticate {
        groupsApi()
    }
}

val Route.dependencies get() = application.dependencies

fun<Error> Raise<Error>.parseUUID(param: String?, error: () -> Error): UUID {
    val groupRaw = ensureNotNull(param, error)
    return catch({ UUID.fromString(groupRaw) }) { raise(error()) }
}