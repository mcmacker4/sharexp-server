@file:UseSerializers(UUIDSerializer::class)
package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID


@Serializable
enum class SplitMethod { EQUAL, AMOUNT, PARTS }

@Serializable
data class ExpenseInfo (
    val id: UUID,
    val title: String,
    val description: String?,

    val paidBy: UUID,
    val splitMethod: SplitMethod,
    val amount: Long,

    val participants: Map<UUID, Long>,
)

@Serializable
data class ExpenseListItem (
    val id: UUID,
    val title: String,
    val paidBy: UUID,
)

@Serializable
data class CreateExpenseRequest (
    val title: String,
    val description: String? = null,

    val paidBy: UUID,
    val splitMethod: SplitMethod,
    val amount: Long,

    val participants: Map<UUID, Long>
)

@Serializable
data class CreateExpenseError (
    val message: String,
    val affectedMembers: Set<UUID>?
)