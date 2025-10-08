@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.app.plugins

import es.hgg.sharexp.persistence.repositories.selectUserIdAndHashByEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
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
                suspendTransaction {
                    selectUserIdAndHashByEmail(credentials.name)?.takeIf {
                        BCrypt.checkpw(credentials.password, it.second)
                    }?.let {
                        UserPrincipal(it.first)
                    }
                }
            }

            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

    }
}