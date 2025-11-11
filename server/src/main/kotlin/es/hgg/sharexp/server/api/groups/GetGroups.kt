package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.model.GroupSort
import es.hgg.sharexp.server.api.getPageRequest
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.persistence.repositories.selectAllVisibleGroups
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.getGroups() = get {
    val principal = call.getUserPrincipal()

    val groups = suspendTransaction {
        val page = call.getPageRequest(GroupSort.MODIFIED)
        selectAllVisibleGroups(page, principal)
    }

    call.respond(groups)
}