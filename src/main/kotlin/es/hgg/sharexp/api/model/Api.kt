package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class NewEntityResponse(val id: Uuid)
