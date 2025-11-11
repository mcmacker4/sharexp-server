package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import es.hgg.sharexp.model.Debt
import es.hgg.sharexp.server.AppError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.min
import kotlin.uuid.Uuid

private val logger: Logger = LoggerFactory.getLogger("DebtSolver")

fun Raise<AppError>.solveDebts(balances: Map<Uuid, Long>): List<Debt> {
    val remainingBalances = balances.filterValues { it != 0L }.toMutableMap()
    val debts = mutableListOf<Debt>()

    // This algorithm should always balance everything out in N-1 iterations, N being the number of people with non-zero balance
    repeat(remainingBalances.size - 1) {
        val (debtor, debtorBalance) = remainingBalances.minBy { it.value }
        val (creditor, creditorBalance) = remainingBalances.maxBy { it.value }

        val amount = min(abs(debtorBalance), abs(creditorBalance))

        debts += Debt(debtor, creditor, amount)

        remainingBalances.replace(debtor, debtorBalance + amount)
        remainingBalances.replace(creditor, creditorBalance - amount)
    }

    if (remainingBalances.any { it.value > 0L }) {
        logger.error("Error solving debts. Balances: {}, Remaining: {}", balances, remainingBalances)
        raise(AppError.Internal)
    }

    return debts
}