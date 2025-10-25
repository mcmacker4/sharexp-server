package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.server.persistence.tables.ExpenseParticipants
import es.hgg.sharexp.server.persistence.tables.Expenses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.r2dbc.select
import java.util.*


/**
 * Returns a Map of all the positive balances. That is, the sum of each expense amount associated to each `paidBy` user
 */
suspend fun selectPositiveBalances(groupId: UUID): Map<UUID, Long> {
    val amount = Expenses.amount.sum()
    val query = Expenses
        .select(Expenses.paidBy, amount)
        .where { Expenses.groupId eq groupId }
        .groupBy(Expenses.paidBy)
        .map { it[Expenses.paidBy] to (it[amount] ?: 0L) }
    return withContext(Dispatchers.IO) { query.toList() }.toMap()
}

suspend fun selectParticipantBalances(groupId: UUID): Map<UUID, Long> {
    val amount = ExpenseParticipants.computedAmount.sum()
    val query = ExpenseParticipants
        .select(ExpenseParticipants.memberId, amount)
        .where { ExpenseParticipants.groupId eq groupId }
        .groupBy(ExpenseParticipants.memberId)
        .map { it[ExpenseParticipants.memberId] to -(it[amount] ?: 0L) }
    return withContext(Dispatchers.IO) { query.toList() }.toMap()
}