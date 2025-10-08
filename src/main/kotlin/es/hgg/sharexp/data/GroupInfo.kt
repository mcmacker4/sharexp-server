@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.data

import es.hgg.sharexp.app.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class GroupInfo(val id: UUID, val name: String, val userMemberId: UUID)

@Serializable
data class GroupInfoWithMembers(val group: GroupInfo, val members: List<MemberInfo>)

@Serializable
data class MemberInfo(val memberId: UUID, val name: String)