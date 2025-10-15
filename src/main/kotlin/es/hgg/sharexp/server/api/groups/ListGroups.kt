package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.selectAllVisibleGroups
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.listGroups() = get {

    val principal = call.principal<UserPrincipal>()
        ?: throw Exception("User must be authenticated")

    val groups = suspendTransaction {
        selectAllVisibleGroups(principal)
    }

    call.respond(groups)

}