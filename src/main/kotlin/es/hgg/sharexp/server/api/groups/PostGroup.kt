package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.api.model.CreateGroupRequest
import es.hgg.sharexp.api.model.NewEntityResponse
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.GroupService
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject

@OptIn(InternalApi::class)
fun Route.postGroup() = post {
    val service by inject<GroupService>()

    val input = call.receive<CreateGroupRequest>()
    respondEither {
        val groupId = suspendTransaction {
            service.createGroup(input.name, call.getUserPrincipal())
        }
        NewEntityResponse(groupId)
    }
}