package es.hgg.sharexp.server.api.groups.members

import arrow.core.raise.Raise
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUUID
import io.ktor.server.routing.*
import java.util.UUID


fun Route.membersApi() = route("/members") {
    getMembers()
    postMember()
    route("/{memberId}") {
        putMember()
        deleteMember()
    }
}

context(raise: Raise<AppError>) fun RoutingCall.getMemberIdParam(): UUID =
    raise.parseUUID(pathParameters["memberId"]) { AppError.BadRequest }
