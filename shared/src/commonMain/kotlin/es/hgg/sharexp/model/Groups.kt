package es.hgg.sharexp.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CreateGroupRequest(val name: String)

@Serializable
data class GroupInfo(val id: Uuid, val name: String, val owner: Uuid, val userMemberId: Uuid)

@Serializable
enum class GroupSort { CREATED, MODIFIED }

