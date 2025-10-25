package es.hgg.sharexp.server.api.groups.balance

import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.fetchGroupBalanceReport
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.getGroupBalance() = get {
    respondEither {
        val principal = call.getUserPrincipal()
        val groupId = getGroupIdParam()

        suspendTransaction { fetchGroupBalanceReport(groupId, principal) }
    }
}