package es.hgg.sharexp.api.groups

import io.ktor.server.routing.Route
import io.ktor.server.routing.route


fun Route.groupsApi() = route("/groups") {
    createGroup()
    route("/{groupId}") {
        fetchGroup()
    }
}