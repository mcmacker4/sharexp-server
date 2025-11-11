package es.hgg.sharexp.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@Serializable
data class BalanceReport(
    val balances: Map<Uuid, Long>,
    val debts: List<Debt>,
)

@Serializable
data class Debt(
    val debtor: Uuid,
    val creditor: Uuid,
    val amount: Long,
)