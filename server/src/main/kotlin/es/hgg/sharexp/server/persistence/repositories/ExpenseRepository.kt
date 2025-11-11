package es.hgg.sharexp.server.persistence.repositories

import es.hgg.sharexp.model.CreateExpenseRequest
import es.hgg.sharexp.model.ExpenseInfo
import es.hgg.sharexp.model.ExpenseListItem
import es.hgg.sharexp.model.ExpenseSort
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.tables.*
import es.hgg.sharexp.server.util.PageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalTime::class)
suspend fun upsertExpense(
    groupId: Uuid,
    expenseId: Uuid?,
    data: CreateExpenseRequest,
    computedAmounts: Map<Uuid, Long>
): Uuid? {
    val now = Clock.System.now()
    return withContext(Dispatchers.IO) {
        Expenses.upsertReturningId(Expenses.id, onUpdateExclude = listOf(Expenses.id, Expenses.createdAt)) {
            if (expenseId != null)
                it[Expenses.id] = expenseId.toJavaUuid()
            it[Expenses.groupId] = groupId.toJavaUuid()
            it[Expenses.title] = data.title
            it[Expenses.description] = data.description
            it[Expenses.createdAt] = now
            it[Expenses.modifiedAt] = now
            it[Expenses.paidBy] = data.paidBy.toJavaUuid()
            it[Expenses.amount] = data.amount
            it[Expenses.splitMethod] = data.splitMethod
        }?.also { expenseId ->
            ExpenseParticipants.deleteWhere { ExpenseParticipants.expenseId eq expenseId }
            ExpenseParticipants.batchInsert(data.participants.entries) { (memberId, amount) ->
                this[ExpenseParticipants.expenseId] = expenseId
                this[ExpenseParticipants.groupId] = groupId.toJavaUuid()
                this[ExpenseParticipants.memberId] = memberId.toJavaUuid()
                this[ExpenseParticipants.configuredAmount] = amount
                this[ExpenseParticipants.computedAmount] = computedAmounts[memberId]!!
            }
        }?.toKotlinUuid()
    }
}

suspend fun selectExpensesList(
    groupId: Uuid,
    request: PageRequest<ExpenseSort>,
    principal: UserPrincipal
): List<ExpenseListItem> = withContext(Dispatchers.IO) {
    Expenses
        .select(Expenses.id, Expenses.title, Expenses.paidBy)
        .where { (Expenses.groupId eq groupId.toJavaUuid()) and groupIsVisibleByUser(principal.userId) }
        .page(request) { it.toSortColumn() }
        .map { ExpenseListItem(it[Expenses.id].toKotlinUuid(), it[Expenses.title], it[Expenses.paidBy].toKotlinUuid()) }
        .toList()
}

@OptIn(ExperimentalTime::class)
private fun ExpenseSort.toSortColumn(): Expression<*> = when (this) {
    ExpenseSort.CREATED -> Expenses.createdAt
    ExpenseSort.MODIFIED -> Expenses.modifiedAt
    ExpenseSort.AMOUNT -> Expenses.amount
}

suspend fun selectExpense(groupId: Uuid, expenseId: Uuid, principal: UserPrincipal): ExpenseInfo? {
    return withContext(Dispatchers.IO) {
        Expenses.selectAll().where {
            (Expenses.groupId eq groupId.toJavaUuid()) and (Expenses.id eq expenseId.toJavaUuid()) and groupIsVisibleByUser(
                principal.userId
            )
        }.map {
            ExpenseInfo(
                id = it[Expenses.id].toKotlinUuid(),
                title = it[Expenses.title],
                description = it[Expenses.description],
                paidBy = it[Expenses.paidBy].toKotlinUuid(),
                splitMethod = it[Expenses.splitMethod],
                amount = it[Expenses.amount],
                participants = selectExpenseParticipants(groupId, expenseId)
            )
        }.singleOrNull()
    }
}

private suspend fun selectExpenseParticipants(groupId: Uuid, expenseId: Uuid): Map<Uuid, Long> {
    return ExpenseParticipants.select(ExpenseParticipants.memberId, ExpenseParticipants.configuredAmount).where {
        (ExpenseParticipants.groupId eq groupId.toJavaUuid()) and (ExpenseParticipants.expenseId eq expenseId.toJavaUuid())
    }.map {
        it[ExpenseParticipants.memberId].toKotlinUuid() to it[ExpenseParticipants.configuredAmount]
    }.toList().toMap()
}

private fun groupIsVisibleByUser(userId: Uuid): Op<Boolean> {
    return exists(GroupMembers.selectAll().where {
        (GroupMembers.groupId eq Expenses.groupId) and (GroupMembers.user eq userId.toJavaUuid())
    })
}
