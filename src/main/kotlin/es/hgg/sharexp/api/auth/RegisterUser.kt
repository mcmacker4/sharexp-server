@file:OptIn(ExperimentalRaiseAccumulateApi::class)

package es.hgg.sharexp.api.auth

import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.withError
import es.hgg.sharexp.service.CreateUserError
import es.hgg.sharexp.service.createUser
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

@Serializable
enum class RegisterError {
    MISSING_PARAM,
    USER_EXISTS,
    INVALID_USERNAME,
    WEAK_PASSWORD,
}

@Serializable
data class RegisterOutput(val error: RegisterError? = null)

fun Route.register() = post("/register") {
    val params = call.receiveParameters()

    suspendTransaction {
        either {
            val username = ensureNotNull(params["username"]) { RegisterError.MISSING_PARAM }
            val email = ensureNotNull(params["email"]) { RegisterError.MISSING_PARAM }
            val password = ensureNotNull(params["password"]) { RegisterError.MISSING_PARAM }

            withError({ it.intoRegisterError() }) {
                createUser(username, email, password)
            }
        }.fold(
            {
                call.respond(
                    if (it == RegisterError.USER_EXISTS) HttpStatusCode.Conflict else HttpStatusCode.BadRequest,
                    RegisterOutput(it)
                )
            },
            {
                call.respond(HttpStatusCode.NoContent)
            }
        )
    }
}

private fun CreateUserError.intoRegisterError() = when (this) {
    CreateUserError.InvalidUserName -> RegisterError.INVALID_USERNAME
    CreateUserError.UserExists -> RegisterError.USER_EXISTS
    CreateUserError.WeakPassword -> RegisterError.WEAK_PASSWORD
}
