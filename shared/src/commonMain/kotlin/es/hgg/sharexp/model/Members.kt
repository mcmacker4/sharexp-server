package es.hgg.sharexp.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class MemberInfo(val memberId: Uuid, val name: String)

@Serializable
data class AddMemberRequest(val name: String)

@Serializable
data class UpdateMemberRequest(val name: String)