package es.hgg.sharexp.server.api.groups.expenses

import es.hgg.sharexp.model.ExpenseSort
import es.hgg.sharexp.server.api.getPageRequest
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.fetchGroupExpenses
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.getExpenses() = get {
    val principal = call.getUserPrincipal()
    respondEither {
        val groupId = call.getGroupIdParam()
        val page = call.getPageRequest(ExpenseSort.CREATED)
        suspendTransaction { fetchGroupExpenses(groupId, page, principal) }
    }
}