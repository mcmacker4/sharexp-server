package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.api.model.GroupSort
import es.hgg.sharexp.server.api.getPageRequest
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.service.GroupService
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject


fun Route.getGroups() = get {
    val principal = call.getUserPrincipal()

    val service by inject<GroupService>()

    val groups = suspendTransaction {
        val page = call.getPageRequest(GroupSort.MODIFIED)
        service.fetchAllVisibleGroups(page, principal)
    }

    call.respond(groups)
}