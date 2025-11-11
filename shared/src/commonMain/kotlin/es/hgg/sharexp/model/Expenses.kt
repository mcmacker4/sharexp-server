package es.hgg.sharexp.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@Serializable
enum class SplitMethod { EQUAL, AMOUNT, PARTS }

@Serializable
data class ExpenseInfo (
    val id: Uuid,
    val title: String,
    val description: String?,

    val paidBy: Uuid,
    val splitMethod: SplitMethod,
    val amount: Long,

    val participants: Map<Uuid, Long>,
)

@Serializable
data class ExpenseListItem (
    val id: Uuid,
    val title: String,
    val paidBy: Uuid,
)

@Serializable
data class CreateExpenseRequest (
    val title: String,
    val description: String? = null,

    val paidBy: Uuid,
    val splitMethod: SplitMethod,
    val amount: Long,

    val participants: Map<Uuid, Long>
)

@Serializable
data class CreateExpenseError (
    val message: String,
    val affectedMembers: Set<Uuid>?
)

@Serializable
enum class ExpenseSort { CREATED, MODIFIED, AMOUNT }
