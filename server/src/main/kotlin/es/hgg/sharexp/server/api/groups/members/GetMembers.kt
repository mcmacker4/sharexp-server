package es.hgg.sharexp.server.api.groups.members

import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.fetchGroupMembers
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.getMembers() = get {
    respondEither {
        val members = suspendTransaction {
            fetchGroupMembers(call.getGroupIdParam(), call.getUserPrincipal())
        }
        ensureNotNull(members) { AppError.NotFound }
    }
}