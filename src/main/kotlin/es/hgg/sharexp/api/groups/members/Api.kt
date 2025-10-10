package es.hgg.sharexp.api.groups.members

import io.ktor.server.routing.*


fun Route.membersApi() = route("/members") {
    listMembers()
    addMember()
    //route("/{memberId}") {
    //    editMember()
    //    deleteMember()
    //}
}