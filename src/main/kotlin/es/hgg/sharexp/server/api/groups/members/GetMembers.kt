package es.hgg.sharexp.server.api.groups.members

import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.GroupMemberService
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject


fun Route.getMembers() = get {
    val service by inject<GroupMemberService>()
    respondEither {
        val members = suspendTransaction {
            service.fetchGroupMembers(call.getGroupIdParam(), call.getUserPrincipal())
        }
        ensureNotNull(members) { AppError.NotFound }
    }
}