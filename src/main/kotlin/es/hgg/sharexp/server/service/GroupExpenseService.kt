package es.hgg.sharexp.server.service

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import es.hgg.sharexp.api.model.CreateExpenseRequest
import es.hgg.sharexp.api.model.ExpenseInfo
import es.hgg.sharexp.api.model.ExpenseListItem
import es.hgg.sharexp.api.model.ExpenseSort
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.ExpenseError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.ExpenseRepository
import es.hgg.sharexp.server.persistence.repositories.GroupMemberRepository
import es.hgg.sharexp.server.persistence.repositories.GroupRepository
import es.hgg.sharexp.server.util.PageRequest
import kotlin.uuid.Uuid


class GroupExpenseService(
    val groupRepo: GroupRepository,
    val expenseRepo: ExpenseRepository,
    val membersRepo: GroupMemberRepository,
    val memberService: GroupMemberService,
) {

    context(raise: Raise<AppError>)
    suspend fun createOrUpdateExpense(
        groupId: Uuid,
        expenseId: Uuid?,
        data: CreateExpenseRequest,
        principal: UserPrincipal
    ): Uuid = with(raise) {
        memberService.ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }

        val expenseMembers = data.participants.keys + data.paidBy
        ensureAllAreMembersOfGroup(groupId, expenseMembers, principal) {
            ExpenseError.InexistentParticipants(it)
        }

        val expenseSolver = createExpenseSolver(data.splitMethod)
        expenseSolver.validate(data.amount, data.participants)?.let {
            raise(it)
        }

        val computedAmounts = expenseSolver.solve(data.paidBy, data.amount, data.participants)

        ensureNotNull(expenseRepo.upsertExpense(groupId, expenseId, data, computedAmounts)) { AppError.Internal }
            .also { groupRepo.updateGroupActivityTimestamp(groupId) }
    }

    context(raise: Raise<AppError>)
    suspend fun fetchGroupExpenses(
        groupId: Uuid,
        pageRequest: PageRequest<ExpenseSort>,
        principal: UserPrincipal
    ): List<ExpenseListItem> = with(raise) {
        memberService.ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }
        return expenseRepo.selectExpensesList(groupId, pageRequest, principal)
    }

    context(raise: Raise<AppError>)
    suspend fun fetchGroupExpense(
        groupId: Uuid,
        expenseId: Uuid,
        principal: UserPrincipal
    ): ExpenseInfo = with(raise) {
        memberService.ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }
        return ensureNotNull(expenseRepo.selectExpense(groupId, expenseId, principal)) { AppError.NotFound }
    }

    context(raise: Raise<E>)
    private suspend inline fun <E> ensureAllAreMembersOfGroup(
        groupId: Uuid,
        members: Set<Uuid>,
        principal: UserPrincipal,
        toError: (Set<Uuid>) -> E
    ): Unit = with(raise) {
        val groupMembers = membersRepo.selectGroupMembers(groupId, principal.userId).map { it.memberId }.toSet()
        val invalidMembers = members.filterTo(mutableSetOf()) { !groupMembers.contains(it) }
        ensure(invalidMembers.isEmpty()) { raise(toError(invalidMembers)) }
    }

}