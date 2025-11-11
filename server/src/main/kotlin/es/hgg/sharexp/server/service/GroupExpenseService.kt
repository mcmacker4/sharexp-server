package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.model.CreateExpenseRequest
import es.hgg.sharexp.model.ExpenseInfo
import es.hgg.sharexp.model.ExpenseListItem
import es.hgg.sharexp.model.ExpenseSort
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.ExpenseError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.*
import es.hgg.sharexp.server.util.PageRequest
import kotlin.uuid.Uuid


suspend fun Raise<AppError>.createOrUpdateExpense(groupId: Uuid, expenseId: Uuid?, data: CreateExpenseRequest, principal: UserPrincipal): Uuid {
    ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }

    val expenseMembers = data.participants.keys + data.paidBy
    ensureAllAreMembersOfGroup(groupId, expenseMembers, principal) {
        ExpenseError.InexistentParticipants(it)
    }

    val expenseSolver = createExpenseSolver(data.splitMethod)
    expenseSolver.validate(data.amount, data.participants)?.let {
        raise(it)
    }

    val computedAmounts = expenseSolver.solve(data.paidBy, data.amount, data.participants)

    return ensureNotNull(upsertExpense(groupId, expenseId, data, computedAmounts)) { AppError.Internal }
        .also { updateGroupActivityTimestamp(groupId) }
}

suspend fun Raise<AppError>.fetchGroupExpenses(groupId: Uuid, pageRequest: PageRequest<ExpenseSort>, principal: UserPrincipal): List<ExpenseListItem> {
    ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }
    return selectExpensesList(groupId, pageRequest, principal)
}

suspend fun Raise<AppError>.fetchGroupExpense(groupId: Uuid, expenseId: Uuid, principal: UserPrincipal): ExpenseInfo {
    ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }
    return ensureNotNull(selectExpense(groupId, expenseId, principal)) { AppError.NotFound }
}

private suspend inline fun<E> Raise<E>.ensureAllAreMembersOfGroup(groupId: Uuid, members: Set<Uuid>, principal: UserPrincipal, toError: (Set<Uuid>) -> E) {
    val groupMembers = selectGroupMembers(groupId, principal.userId).map { it.memberId }.toSet()
    val invalidMembers = members.filterTo(mutableSetOf()) { !groupMembers.contains(it) }
    ensure(invalidMembers.isEmpty()) { raise(toError(invalidMembers)) }
}