package es.hgg.sharexp.api.groups

import es.hgg.sharexp.api.groups.members.membersApi
import io.ktor.server.routing.Route
import io.ktor.server.routing.route


fun Route.groupsApi() = route("/groups") {
    createGroup()

    listGroups()

    route("/{groupId}") {
        fetchGroup()

        membersApi()
    }
}