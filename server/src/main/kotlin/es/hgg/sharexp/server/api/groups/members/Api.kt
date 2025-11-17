package es.hgg.sharexp.server.api.groups.members

import arrow.core.raise.Raise
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUuid
import io.ktor.server.routing.*
import kotlin.uuid.Uuid


fun Route.membersApi() = route("/members") {
    getMembers()
    postMember()
    route("/{memberId}") {
        putMember()
        deleteMember()
    }
}

context(raise: Raise<AppError>) fun RoutingCall.getMemberIdParam(): Uuid =
    raise.parseUuid(pathParameters["memberId"]) { AppError.BadRequest }
