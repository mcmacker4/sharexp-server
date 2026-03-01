package es.hgg.sharexp.server.api.groups.expenses

import es.hgg.sharexp.api.model.ExpenseSort
import es.hgg.sharexp.server.api.getPageRequest
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.GroupExpenseService
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject


fun Route.getExpenses() = get {
    val principal = call.getUserPrincipal()

    val service by inject<GroupExpenseService>()

    respondEither {
        val groupId = call.getGroupIdParam()
        val page = call.getPageRequest(ExpenseSort.CREATED)
        suspendTransaction { service.fetchGroupExpenses(groupId, page, principal) }
    }
}