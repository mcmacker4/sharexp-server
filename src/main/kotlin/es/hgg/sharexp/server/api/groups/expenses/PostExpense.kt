package es.hgg.sharexp.server.api.groups.expenses

import arrow.core.raise.either
import es.hgg.sharexp.api.model.CreateExpenseError
import es.hgg.sharexp.api.model.CreateExpenseRequest
import es.hgg.sharexp.api.model.NewEntityResponse
import es.hgg.sharexp.server.ExpenseError
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.groups.getGroupIdParam
import es.hgg.sharexp.server.service.createOrUpdateExpense
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.postExpense() = post {
    val principal = call.getUserPrincipal()

    either {
        val req = call.receive<CreateExpenseRequest>()

        val expenseId = suspendTransaction {
            createOrUpdateExpense(getGroupIdParam(), null, req, principal)
        }

        NewEntityResponse(expenseId)
    }.fold({ error ->
        when (error) {
            is ExpenseError -> call.respond(
                error.status,
                CreateExpenseError(error.message, error.affectedParticipants)
            )
            else -> call.respond(error.status)
        }
    }, { call.respond(it) })
}

/*

suspendTransaction {

    val users = withContext(Dispatchers.IO) {
        // Select users using exposed
    }

    withContext(Dispatchers.IO) {
        // Update some other table using exposed
    }

}

 */