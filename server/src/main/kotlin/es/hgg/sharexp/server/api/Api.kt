package es.hgg.sharexp.server.api

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.auth.authentication
import es.hgg.sharexp.server.api.groups.groupsApi
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.util.PageRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlin.uuid.Uuid

fun Application.configureApi() = routing {
    authentication()
    authenticate {
        groupsApi()
    }
}

fun <Error> Raise<Error>.parseUuid(param: String?, error: () -> Error): Uuid {
    val groupRaw = ensureNotNull(param, error)
    return catch({ Uuid.parse(groupRaw) }) { raise(error()) }
}

suspend inline fun RoutingContext.respondIfError(block: Raise<AppError>.() -> Unit) {
    either(block).fold({ call.respond(it.status) }, { call.respond(HttpStatusCode.NoContent) })
}

suspend inline fun<reified T> RoutingContext.respondEither(block: Raise<AppError>.() -> T) {
    either(block).fold({ call.respond(it.status) }, { call.respond(it, typeInfo<T>()) })
}

fun ApplicationCall.getUserPrincipal(): UserPrincipal =
    principal<UserPrincipal>() ?: throw Exception("User must be authenticated")

inline fun<reified S : Enum<S>> RoutingCall.getPageRequest(defaultSort: S, defaultPage: Int = 1, defaultSize: Int = 10, ascending: Boolean = false): PageRequest<S> = PageRequest(
    queryParameters["page"]?.toIntOrNull() ?: defaultPage,
    queryParameters["size"]?.toIntOrNull() ?: defaultSize,
    queryParameters["sort"]?.let { runCatching { enumValueOf<S>(it) }.getOrNull() } ?: defaultSort,
    queryParameters["asc"]?.toBooleanStrict() ?: ascending,
)