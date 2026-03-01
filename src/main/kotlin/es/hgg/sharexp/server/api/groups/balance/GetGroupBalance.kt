package es.hgg.sharexp.server.api.groups.balance

import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.BalanceService
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject


fun Route.getGroupBalance() = get {
    respondEither {
        val principal = call.getUserPrincipal()
        val groupId = call.getGroupIdParam()

        val service by inject<BalanceService>()

        suspendTransaction { service.fetchGroupBalanceReport(groupId, principal) }
    }
}