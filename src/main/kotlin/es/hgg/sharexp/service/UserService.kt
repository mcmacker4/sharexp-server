package es.hgg.sharexp.service

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.persistence.repositories.UserRepository
import org.mindrot.jbcrypt.BCrypt.gensalt
import org.mindrot.jbcrypt.BCrypt.hashpw
import java.util.*

private val ALLOWED_USERNAME_CHARS: Set<Char> = setOf('.', '_', '-')

class UserService(val repository: UserRepository) {

    sealed interface CreateUserError
    object UserExists : CreateUserError
    object InvalidUserName : CreateUserError
    object WeakPassword : CreateUserError

    context(raise: Raise<CreateUserError>)
    suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Unit = with(raise) {
        validateUserData(username, password)

        val newId = repository.createUser(
            username = username,
            email = email,
            pwHash = hashpw(password, gensalt()),
        )

        ensureNotNull(newId) { UserExists }
    }

    context(raise: Raise<CreateUserError>)
    private fun validateUserData(username: String, password: String) = with(raise) {
        ensure(username.length > 3) { InvalidUserName }
        ensure(username.first().isLetter()) { InvalidUserName }
        ensure(username.all { it.isLetterOrDigit() || ALLOWED_USERNAME_CHARS.contains(it) }) { InvalidUserName }

        ensure(password.length > 6) { WeakPassword }
        ensure(password.any { it.isDigit() }) { WeakPassword }
        ensure(password.any { it.isLetter() }) { WeakPassword }
        ensure(password.any { !it.isDigit() && !it.isLetter() }) { WeakPassword }
    }

    suspend fun getUserDisplayName(userId: UUID): String? =
        repository.fetchUserDisplayName(userId)

}