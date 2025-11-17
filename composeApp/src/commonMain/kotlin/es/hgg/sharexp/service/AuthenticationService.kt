package es.hgg.sharexp.service

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import es.hgg.sharexp.model.RegisterError.INVALID_USERNAME
import es.hgg.sharexp.model.RegisterError.MISSING_PARAM
import es.hgg.sharexp.model.RegisterError.USER_EXISTS
import es.hgg.sharexp.model.RegisterError.WEAK_PASSWORD
import es.hgg.sharexp.model.RegisterOutput
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jetbrains.compose.resources.getString
import sharexp.composeapp.generated.resources.Res
import sharexp.composeapp.generated.resources.api_base_url

sealed interface LoginError {
    object Authentication : LoginError
    object Internal : LoginError
}

sealed interface RegisterError {
    object Internal : RegisterError
    object InvalidUsername : RegisterError
    object WeakPassword : RegisterError
    object UserExists : RegisterError
    object EmptyField : RegisterError
}

class AuthenticationService(
    val client: HttpClient,
) {

    suspend fun fetchAuthStatus(): Either<LoginError, Unit> = either {
        client.get("${getBaseUrl()}/auth/status").raiseLoginError()
    }

    suspend fun login(email: String, password: String): Either<LoginError, Unit> = either {
        val base = getString(Res.string.api_base_url)
        client.submitForm(
            url = "$base/auth/login",
            formParameters = parametersOf(
                "email" to listOf(email),
                "password" to listOf(password),
            )
        ).raiseLoginError()
    }

    suspend fun register(email: String, username: String, password: String): Either<RegisterError, Unit> = either {
        val base = getString(Res.string.api_base_url)
        client.submitForm(
            url = "$base/auth/register",
            formParameters = parametersOf(
                "email" to listOf(email),
                "username" to listOf(username),
                "password" to listOf(password),
            )
        ).raiseRegisterError()
    }

}

context(raise: Raise<LoginError>)
private fun HttpResponse.raiseLoginError() = when (status) {
    HttpStatusCode.Found, HttpStatusCode.OK -> Unit
    HttpStatusCode.Unauthorized -> raise.raise(LoginError.Authentication)
    else -> raise.raise(LoginError.Internal)
}

context(raise: Raise<RegisterError>)
private suspend fun HttpResponse.raiseRegisterError() = when (status) {
    HttpStatusCode.Found, HttpStatusCode.OK -> Unit
    HttpStatusCode.BadRequest, HttpStatusCode.Conflict -> raise.raise(intoInvalidDataError())
    else -> raise.raise(RegisterError.Internal)
}

private suspend fun HttpResponse.intoInvalidDataError(): RegisterError = when (body<RegisterOutput>().error) {
    MISSING_PARAM -> RegisterError.EmptyField
    USER_EXISTS -> RegisterError.UserExists
    INVALID_USERNAME -> RegisterError.InvalidUsername
    WEAK_PASSWORD -> RegisterError.WeakPassword
    null -> RegisterError.Internal
}
