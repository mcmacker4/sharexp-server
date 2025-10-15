package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.server.persistence.repositories.insertUser
import org.mindrot.jbcrypt.BCrypt.gensalt
import org.mindrot.jbcrypt.BCrypt.hashpw

private val ALLOWED_USERNAME_CHARS: Set<Char> = setOf('.', '_', '-')

sealed interface CreateUserError {
    object UserExists : CreateUserError
    object InvalidUserName : CreateUserError
    object WeakPassword : CreateUserError
}

suspend fun Raise<CreateUserError>.createUser(
    username: String,
    email: String,
    password: String,
) {
    validateUserData(username, password)

    val newId = insertUser(
        username = username,
        email = email,
        pwHash = hashpw(password, gensalt()),
    )

    ensureNotNull(newId) { CreateUserError.UserExists }
}

private fun Raise<CreateUserError>.validateUserData(username: String, password: String) {
    ensure(username.length > 3) { CreateUserError.InvalidUserName }
    ensure(username.first().isLetter()) { CreateUserError.InvalidUserName }
    ensure(username.all { it.isLetterOrDigit() || ALLOWED_USERNAME_CHARS.contains(it) }) { CreateUserError.InvalidUserName }

    ensure(password.length > 6) { CreateUserError.WeakPassword }
    ensure(password.any { it.isDigit() }) { CreateUserError.WeakPassword }
    ensure(password.any { it.isLetter() }) { CreateUserError.WeakPassword }
    ensure(password.any { !it.isDigit() && !it.isLetter() }) { CreateUserError.WeakPassword }
}