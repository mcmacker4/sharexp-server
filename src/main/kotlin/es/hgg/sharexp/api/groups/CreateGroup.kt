@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.api.groups

import es.hgg.sharexp.api.dependencies
import es.hgg.sharexp.app.plugins.UUIDSerializer
import es.hgg.sharexp.app.plugins.UserPrincipal
import es.hgg.sharexp.service.GroupService
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID


@Serializable
data class CreateGroupInput(val name: String)

@Serializable
data class CreateGroupOutput(val id: UUID)

fun Route.createGroup() = post {
    val service: GroupService by dependencies

    val principal = call.principal<UserPrincipal>()
        ?: throw Exception("User must be authenticated")

    val input = call.receive<CreateGroupInput>()

    val groupId = service.createGroup(input.name, principal)

    call.respond(CreateGroupOutput(groupId))
}