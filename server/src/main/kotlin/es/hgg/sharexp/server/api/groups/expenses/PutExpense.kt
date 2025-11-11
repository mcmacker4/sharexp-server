package es.hgg.sharexp.server.api.groups.expenses

import arrow.core.raise.either
import es.hgg.sharexp.model.CreateExpenseError
import es.hgg.sharexp.model.CreateExpenseRequest
import es.hgg.sharexp.server.ExpenseError
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.service.createOrUpdateExpense
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.putExpense() = put {
    val principal = call.getUserPrincipal()

    either {
        val req = call.receive<CreateExpenseRequest>()

        suspendTransaction {
            createOrUpdateExpense(call.getGroupIdParam(), call.getExpenseIdParam(), req, principal)
        }
    }.fold({ error ->
        when (error) {
            is ExpenseError -> call.respond(
                error.status,
                CreateExpenseError(error.message, error.affectedParticipants)
            )
            else -> call.respond(error.status)
        }
    }, { call.respond(HttpStatusCode.OK) })
}