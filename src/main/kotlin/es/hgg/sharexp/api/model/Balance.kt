@file:UseSerializers(UUIDSerializer::class)

package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*


@Serializable
data class BalanceReport(
    val balances: Map<UUID, Long>,
    val debts: List<Debt>,
)

@Serializable
data class Debt(
    val debtor: UUID,
    val creditor: UUID,
    val amount: Long,
)