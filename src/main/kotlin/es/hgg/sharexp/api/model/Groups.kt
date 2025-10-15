@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class CreateGroupRequest(val name: String)

@Serializable
data class CreateGroupResponse(val id: UUID)

@Serializable
data class GroupInfo(val id: UUID, val name: String, val owner: UUID, val userMemberId: UUID)

