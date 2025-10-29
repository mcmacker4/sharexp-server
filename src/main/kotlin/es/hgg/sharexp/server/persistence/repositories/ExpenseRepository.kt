package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.api.model.CreateExpenseRequest
import es.hgg.sharexp.api.model.ExpenseInfo
import es.hgg.sharexp.api.model.ExpenseListItem
import es.hgg.sharexp.api.model.ExpenseSort
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.tables.ExpenseParticipants
import es.hgg.sharexp.server.persistence.tables.Expenses
import es.hgg.sharexp.server.persistence.tables.GroupMembers
import es.hgg.sharexp.server.persistence.tables.page
import es.hgg.sharexp.server.persistence.tables.upsertReturningId
import es.hgg.sharexp.server.util.PageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.exists
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend fun upsertExpense(
    groupId: UUID,
    expenseId: UUID?,
    data: CreateExpenseRequest,
    computedAmounts: Map<UUID, Long>
): UUID? {
    val now = Clock.System.now()
    return withContext(Dispatchers.IO) {
        Expenses.upsertReturningId(Expenses.id, onUpdateExclude = listOf(Expenses.id, Expenses.createdAt)) {
            if (expenseId != null)
                it[Expenses.id] = expenseId
            it[Expenses.groupId] = groupId
            it[Expenses.title] = data.title
            it[Expenses.description] = data.description
            it[Expenses.createdAt] = now
            it[Expenses.modifiedAt] = now
            it[Expenses.paidBy] = data.paidBy
            it[Expenses.amount] = data.amount
            it[Expenses.splitMethod] = data.splitMethod
        }?.also { expenseId ->
            ExpenseParticipants.deleteWhere { ExpenseParticipants.expenseId eq expenseId }
            ExpenseParticipants.batchInsert(data.participants.entries) { (memberId, amount) ->
                this[ExpenseParticipants.expenseId] = expenseId
                this[ExpenseParticipants.groupId] = groupId
                this[ExpenseParticipants.memberId] = memberId
                this[ExpenseParticipants.configuredAmount] = amount
                this[ExpenseParticipants.computedAmount] = computedAmounts[memberId]!!
            }
        }
    }
}

suspend fun selectExpensesList(groupId: UUID, request: PageRequest<ExpenseSort>, principal: UserPrincipal): List<ExpenseListItem> {
    return Expenses
        .select(Expenses.id, Expenses.title, Expenses.paidBy)
        .where { (Expenses.groupId eq groupId) and groupIsVisibleByUser(principal.userId) }
        .page(request) { it.toSortColumn() }
        .map { ExpenseListItem(it[Expenses.id], it[Expenses.title], it[Expenses.paidBy]) }
        .toList()
}

@OptIn(ExperimentalTime::class)
private fun ExpenseSort.toSortColumn(): Expression<*> = when (this) {
    ExpenseSort.CREATED -> Expenses.createdAt
    ExpenseSort.MODIFIED -> Expenses.modifiedAt
    ExpenseSort.AMOUNT -> Expenses.amount
}

suspend fun selectExpense(groupId: UUID, expenseId: UUID, principal: UserPrincipal): ExpenseInfo? {
    return withContext(Dispatchers.IO) {
        Expenses.selectAll().where {
            (Expenses.groupId eq groupId) and (Expenses.id eq expenseId) and groupIsVisibleByUser(principal.userId)
        }.map {
            ExpenseInfo(
                id = it[Expenses.id],
                title = it[Expenses.title],
                description = it[Expenses.description],
                paidBy = it[Expenses.paidBy],
                splitMethod = it[Expenses.splitMethod],
                amount = it[Expenses.amount],
                participants = selectExpenseParticipants(groupId, expenseId)
            )
        }.singleOrNull()
    }
}

private suspend fun selectExpenseParticipants(groupId: UUID, expenseId: UUID): Map<UUID, Long> {
    return ExpenseParticipants.select(ExpenseParticipants.memberId, ExpenseParticipants.configuredAmount).where {
        (ExpenseParticipants.groupId eq groupId) and (ExpenseParticipants.expenseId eq expenseId)
    }.map {
        it[ExpenseParticipants.memberId] to it[ExpenseParticipants.configuredAmount]
    }.toList().toMap()
}

private fun groupIsVisibleByUser(userId: UUID): Op<Boolean> {
    return exists(GroupMembers.selectAll().where {
        (GroupMembers.groupId eq Expenses.groupId) and (GroupMembers.user eq userId)
    })
}
