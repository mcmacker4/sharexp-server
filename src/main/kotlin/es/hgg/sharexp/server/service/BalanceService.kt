package es.hgg.sharexp.server.service

import arrow.core.combine
import arrow.core.raise.Raise
import es.hgg.sharexp.api.model.BalanceReport
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.selectParticipantBalances
import es.hgg.sharexp.server.persistence.repositories.selectPositiveBalances
import java.util.UUID


suspend fun Raise<AppError>.fetchGroupBalanceReport(groupId: UUID, principal: UserPrincipal): BalanceReport {
    ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }

    val positiveBalances = selectPositiveBalances(groupId)
    val negativeBalances = selectParticipantBalances(groupId)

    val balances = positiveBalances.combine(negativeBalances, Long::plus)
    val debts = solveDebts(balances)

    return BalanceReport(balances, debts)
}