package es.hgg.sharexp.server.api.groups.expenses

import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.fetchGroupExpense
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.getExpense() = get {
    val principal = call.getUserPrincipal()

    respondEither {
        val groupId = getGroupIdParam()
        val expenseId = getExpenseIdParam()

        suspendTransaction { fetchGroupExpense(groupId, expenseId, principal) }
    }
}