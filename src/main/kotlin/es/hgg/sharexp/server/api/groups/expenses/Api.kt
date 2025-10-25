package es.hgg.sharexp.server.api.groups.expenses

import arrow.core.raise.Raise
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.api.parseUUID
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.route


fun Route.expensesApi() = route("/expenses") {
    getExpenses()
    postExpense()

    route("/{expenseId}") {
        getExpense()
        putExpense()
    }
}

context(route: RoutingContext) fun Raise<AppError>.getExpenseIdParam() =
    parseUUID(route.call.pathParameters["expenseId"]) { AppError.BadRequest }