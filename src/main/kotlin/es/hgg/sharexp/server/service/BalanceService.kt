package es.hgg.sharexp.server.service

import arrow.core.combine
import arrow.core.raise.Raise
import es.hgg.sharexp.api.model.BalanceReport
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.app.plugins.UserPrincipal
import es.hgg.sharexp.server.persistence.repositories.BalanceRepository
import kotlin.uuid.Uuid

class BalanceService(
    val memberService: GroupMemberService,
    val repo: BalanceRepository,
    val solver: DebtSolver,
) {

    context(_: Raise<AppError>)
    suspend fun fetchGroupBalanceReport(groupId: Uuid, principal: UserPrincipal): BalanceReport {
        memberService.ensureUserIsGroupMember(groupId, principal) { AppError.NotFound }

        val positiveBalances = repo.selectPositiveBalances(groupId)
        val negativeBalances = repo.selectParticipantBalances(groupId)

        val balances = positiveBalances.combine(negativeBalances, Long::plus)
        val debts = solver.solveDebts(balances)

        return BalanceReport(balances, debts)
    }

}