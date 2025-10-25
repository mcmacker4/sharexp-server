@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class NewEntityResponse(val id: UUID)
