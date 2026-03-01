package es.hgg.sharexp.server.api.groups.expenses

import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.GroupExpenseService
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject


fun Route.getExpense() = get {
    val principal = call.getUserPrincipal()

    val service by inject<GroupExpenseService>()

    respondEither {
        val groupId = call.getGroupIdParam()
        val expenseId = call.getExpenseIdParam()

        suspendTransaction { service.fetchGroupExpense(groupId, expenseId, principal) }
    }
}