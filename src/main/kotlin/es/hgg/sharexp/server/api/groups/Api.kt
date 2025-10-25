package es.hgg.sharexp.server.api.groups

import arrow.core.raise.Raise
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.groups.balance.balanceApi
import es.hgg.sharexp.server.api.groups.expenses.expensesApi
import es.hgg.sharexp.server.api.groups.members.membersApi
import es.hgg.sharexp.server.api.parseUUID
import io.ktor.server.routing.*
import java.util.UUID


fun Route.groupsApi() = route("/groups") {
    postGroup()

    getGroups()

    route("/{groupId}") {
        getGroup()

        membersApi()
        expensesApi()
        balanceApi()
    }
}

context(route: RoutingContext) fun Raise<AppError>.getGroupIdParam(): UUID =
    parseUUID(route.call.pathParameters["groupId"]) { AppError.BadRequest }