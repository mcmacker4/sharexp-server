package es.hgg.sharexp.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class EntityIdResponse(val id: Uuid)
