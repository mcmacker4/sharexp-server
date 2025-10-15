package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.server.api.groups.members.membersApi
import io.ktor.server.routing.*


fun Route.groupsApi() = route("/groups") {
    createGroup()

    listGroups()

    route("/{groupId}") {
        fetchGroup()

        membersApi()
    }
}