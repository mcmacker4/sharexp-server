package es.hgg.sharexp.server.app.plugins

import es.hgg.sharexp.server.persistence.repositories.selectUserIdAndHashByEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import kotlin.uuid.Uuid

@Serializable
data class UserPrincipal(val userId: Uuid)

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