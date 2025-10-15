@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class MemberInfo(val memberId: UUID, val name: String)

@Serializable
data class AddMemberRequest(val name: String)

@Serializable
data class AddMemberResponse(val id: UUID)