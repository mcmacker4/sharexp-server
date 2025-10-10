package es.hgg.sharexp.api.groups.members

import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.api.parseUUID
import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.data.MemberInfo
import es.hgg.sharexp.service.fetchGroupMembers
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


sealed interface ListMembersError {
    object BadRequest : ListMembersError
    object GroupNotFound : ListMembersError
}

fun Route.listMembers() = get {
    either {
        val groupId = parseUUID(call.pathParameters["groupId"]) { ListMembersError.BadRequest }

        val principal = call.principal<UserPrincipal>()
            ?: throw Exception("User must be authenticated")

        ensureNotNull(suspendTransaction {
            fetchGroupMembers(groupId, principal)
        }) { ListMembersError.GroupNotFound }
    }.fold({ error ->
        call.respond(when (error) {
            is ListMembersError.BadRequest -> HttpStatusCode.BadRequest
            is ListMembersError.GroupNotFound -> HttpStatusCode.NotFound
        })
    }, { members: List<MemberInfo> ->
        call.respond(members)
    })
}