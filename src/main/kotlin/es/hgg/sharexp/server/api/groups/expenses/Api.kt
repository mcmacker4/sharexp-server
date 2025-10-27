package es.hgg.sharexp.server.api.groups.expenses

import arrow.core.raise.Raise
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUUID
import io.ktor.server.routing.*


fun Route.expensesApi() = route("/expenses") {
    getExpenses()
    postExpense()

    route("/{expenseId}") {
        getExpense()
        putExpense()
    }
}

context(raise: Raise<AppError>) fun RoutingCall.getExpenseIdParam() =
    raise.parseUUID(pathParameters["expenseId"]) { AppError.BadRequest }