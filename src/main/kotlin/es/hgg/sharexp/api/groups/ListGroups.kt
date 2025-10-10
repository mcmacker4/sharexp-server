package es.hgg.sharexp.api.groups

import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.persistence.repositories.selectAllVisibleGroups
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.listGroups() = get {

    val principal = call.principal<UserPrincipal>()
        ?: throw Exception("Must be authenticated")

    val groups = suspendTransaction {
        selectAllVisibleGroups(principal)
    }

    call.respond(HttpStatusCode.OK, groups)

}