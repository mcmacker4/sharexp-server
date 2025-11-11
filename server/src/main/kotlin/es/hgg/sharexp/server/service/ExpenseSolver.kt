package es.hgg.sharexp.server.service

import es.hgg.sharexp.model.SplitMethod
import es.hgg.sharexp.server.ExpenseError
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.uuid.Uuid

/**
 * Creates a solver depending on the chosen split method.
 */
fun createExpenseSolver(
    splitMethod: SplitMethod,
    luckySelector: LuckyParticipantSelector = LuckyParticipantSelector.Random
) = when (splitMethod) {
    SplitMethod.EQUAL -> EqualAmountsSolver(luckySelector)
    SplitMethod.AMOUNT -> SpecificAmountsSolver()
    SplitMethod.PARTS -> PartsSolver(luckySelector)
}

sealed interface ExpenseSolver {
    fun validate(totalAmount: Long, participants: Map<Uuid, Long>): ExpenseError?
    fun solve(paidBy: Uuid, totalAmount: Long, participants: Map<Uuid, Long>): Map<Uuid, Long>
}

private class EqualAmountsSolver(luckySelector: LuckyParticipantSelector) : PartsSolver(luckySelector) {
    override fun solve(paidBy: Uuid, totalAmount: Long, participants: Map<Uuid, Long>): Map<Uuid, Long> {
        return super.solve(paidBy, totalAmount, participants.mapValues { 1 })
    }
}

private class SpecificAmountsSolver : ExpenseSolver {
    override fun validate(totalAmount: Long, participants: Map<Uuid, Long>): ExpenseError? {
        return commonValidations(totalAmount, participants)
    }

    override fun solve(paidBy: Uuid, totalAmount: Long, participants: Map<Uuid, Long>): Map<Uuid, Long> = participants
}

open class PartsSolver(val luckySelector: LuckyParticipantSelector) : ExpenseSolver {
    override fun validate(totalAmount: Long, participants: Map<Uuid, Long>): ExpenseError? {
        commonValidations(totalAmount, participants)?.let { return it }

        if (participants.values.sum() > totalAmount)
            return ExpenseError.TooManyParts // It would amount to less than one cent per part

        return null
    }

    override fun solve(paidBy: Uuid, totalAmount: Long, participants: Map<Uuid, Long>): Map<Uuid, Long> {
        val parts = participants.values.sum()

        val total = BigDecimal(totalAmount.toBigInteger(), 2)
        val partsDecimal = BigDecimal(parts.toBigInteger(), 0)

        // We round up and the extra cents are then subtracted from a lucky participant
        // This ensures that the sum of all the computed amounts for the participants is exactly the total amount
        // regardless of rounding errors

        val split = total.divide(partsDecimal, 2, RoundingMode.CEILING)
        val amountPerPart = split.unscaledValue().toLong()

        // Example:
        // Expense of 17.73 paid by Alice
        // - Bob:   1 part -> Owes: 3.55
        // - Alice: 2 parts -> Owes: 7.08
        // - Steve: 2 parts -> Owes: 7.10

        val remainder = (amountPerPart * parts) - totalAmount


        // If whoever paid is also a participant, they will be the lucky participant who is pardoned the rounding errors.
        // Otherwise, a participant is chosen by the `luckySelector`.
        val lucky = if (paidBy in participants) paidBy else luckySelector.select(paidBy, participants.keys)

        return participants.mapValues { (member, parts) ->
            if (member == lucky)
                (amountPerPart * parts) - remainder
            else
                (amountPerPart * parts)
        }
    }
}

interface LuckyParticipantSelector {
    fun select(paidBy: Uuid, participants: Set<Uuid>): Uuid

    object Random : LuckyParticipantSelector {
        override fun select(paidBy: Uuid, participants: Set<Uuid>): Uuid = participants.random()
    }
}

private fun commonValidations(totalAmount: Long, participants: Map<Uuid, Long>): ExpenseError? {
    if (participants.isEmpty())
        return ExpenseError.NoParticipants

    if (totalAmount <= 0L)
        return ExpenseError.InvalidTotalAmount

    val participantsWithInvalidAmount = participants.filterValues { it <= 0L }.keys
    if (participantsWithInvalidAmount.isNotEmpty())
        return ExpenseError.InvalidParticipantAmount(participantsWithInvalidAmount)

    return null
}