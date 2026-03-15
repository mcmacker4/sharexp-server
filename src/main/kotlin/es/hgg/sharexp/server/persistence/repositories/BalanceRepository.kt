package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.server.persistence.tables.ExpenseParticipants
import es.hgg.sharexp.server.persistence.tables.Expenses
import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.r2dbc.select
import kotlin.uuid.Uuid


class BalanceRepository {
    /**
     * Returns a Map of all the positive balances. That is, the sum of each expense amount associated to each `paidBy` user
     */
    suspend fun selectPositiveBalances(groupId: Uuid): Map<Uuid, Long> {
        val amount = Expenses.amount.sum()
        return Expenses
            .select(Expenses.paidBy, amount)
            .where { Expenses.groupId eq groupId }
            .groupBy(Expenses.paidBy)
            .associate { it[Expenses.paidBy] to (it[amount] ?: 0L) }
    }

    suspend fun selectParticipantBalances(groupId: Uuid): Map<Uuid, Long> {
        val amount = ExpenseParticipants.computedAmount.sum()
        return ExpenseParticipants
            .select(ExpenseParticipants.memberId, amount)
            .where { ExpenseParticipants.groupId eq groupId }
            .groupBy(ExpenseParticipants.memberId)
            .associate { it[ExpenseParticipants.memberId] to -(it[amount] ?: 0L) }
    }

}

suspend fun<T, K, V> Flow<T>.associate(transform: (T) -> Pair<K, V>) =
    buildMap {
        collect {
            this += transform(it)
        }
    }