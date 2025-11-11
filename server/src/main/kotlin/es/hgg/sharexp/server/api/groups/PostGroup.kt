package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.model.CreateGroupRequest
import es.hgg.sharexp.model.EntityIdResponse
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.createGroup
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

@OptIn(InternalApi::class)
fun Route.postGroup() = post {
    val input = call.receive<CreateGroupRequest>()
    respondEither {
        val groupId = suspendTransaction {
            createGroup(input.name, call.getUserPrincipal())
        }
        EntityIdResponse(groupId)
    }
}