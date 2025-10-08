@file:OptIn(ExperimentalRaiseAccumulateApi::class)

package es.hgg.sharexp.api.auth

import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.withError
import es.hgg.sharexp.api.dependencies
import es.hgg.sharexp.service.UserService
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
    val service: UserService by dependencies

    val params = call.receiveParameters()

    suspendTransaction {
        either {
            val username = ensureNotNull(params["username"]) { RegisterError.MISSING_PARAM }
            val email = ensureNotNull(params["email"]) { RegisterError.MISSING_PARAM }
            val password = ensureNotNull(params["password"]) { RegisterError.MISSING_PARAM }

            withError({ it.intoRegisterError() }) {
                service.createUser(username, email, password)
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

private fun UserService.CreateUserError.intoRegisterError() = when (this) {
    UserService.InvalidUserName -> RegisterError.INVALID_USERNAME
    UserService.UserExists -> RegisterError.USER_EXISTS
    UserService.WeakPassword -> RegisterError.WEAK_PASSWORD
}
