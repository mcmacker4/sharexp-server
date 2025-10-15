package es.hgg.sharexp.server.api

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.auth.authentication
import es.hgg.sharexp.server.api.groups.groupsApi
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import java.util.*

fun Application.configureApi() = routing {
    authentication()
    authenticate {
        groupsApi()
    }
}

fun <Error> Raise<Error>.parseUUID(param: String?, error: () -> Error): UUID {
    val groupRaw = ensureNotNull(param, error)
    return catch({ UUID.fromString(groupRaw) }) { raise(error()) }
}

suspend inline fun<reified T> RoutingContext.respondEither(block: Raise<AppError>.() -> T) {
    either(block).fold({ call.respond(it.status) }, { call.respond(it, typeInfo<T>()) })
}
