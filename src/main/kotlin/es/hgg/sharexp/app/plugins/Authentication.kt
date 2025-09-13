@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.app.plugins

import es.hgg.sharexp.persistence.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.mindrot.jbcrypt.BCrypt
import java.util.*

@Serializable
data class UserPrincipal(val userId: UUID)

fun Application.configureAuthentication() {
    install(Authentication) {

        session<UserPrincipal> {
            validate { it }
            challenge { call.respond(HttpStatusCode.Unauthorized) }
        }

        form("form") {
            userParamName = "email"
            passwordParamName = "password"

            validate { credentials ->
                val repository: UserRepository by this@configureAuthentication.dependencies

                repository.fetchIdAndHashByEmail(credentials.name)?.takeIf {
                    BCrypt.checkpw(credentials.password, it.second)
                }?.let {
                    UserPrincipal(it.first)
                }
            }

            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

    }
}