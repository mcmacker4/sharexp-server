import arrow.core.Either
import arrow.core.raise.either
import es.hgg.sharexp.api.model.Debt
import es.hgg.sharexp.server.AppError
import es.hgg.sharexp.server.service.DebtSolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class BalanceReportTest {

    companion object {
        val BOB = Uuid.generateV7()
        val ALICE = Uuid.generateV7()
        val STEVE = Uuid.generateV7()
    }

    val solver = DebtSolver()

    @Test
    fun `all zero balances, nothing to report`() {
        either {
            solver.solveDebts(mapOf(BOB to 0L, ALICE to 0L))
        }.shouldSucceed {
            assertEquals(emptyList(), it)
        }
    }

    @Test
    fun `uneven balances should result in error`() {
        either {
            solver.solveDebts(mapOf(ALICE to 20, BOB to -10))
        }.shouldFail {
            assertEquals(AppError.Internal, it)
        }
    }

    @Test
    fun `one debt`() {
        either {
            solver.solveDebts(mapOf(ALICE to 10, BOB to -10))
        }.shouldSucceed {
            assertEquals(listOf(Debt(BOB, ALICE, 10L)), it)
        }
    }

    @Test
    fun `two debts`() {
        either {
            solver.solveDebts(mapOf(ALICE to 1L, STEVE to -3L, BOB to 2L))
        }.shouldSucceed {
            assertEquals(listOf(
                Debt(STEVE, BOB, 2L),
                Debt(STEVE, ALICE, 1L)
            ), it)
        }
    }

    fun<L, R> Either<L, R>.shouldFail(assertion: (L) -> Unit) =
        fold({ assertion(it) }, { assert(false) { "Should be an error" } })

    fun<L, R> Either<L, R>.shouldSucceed(assertion: (R) -> Unit) =
        fold({ assert(false) { "Should have succeeded" } }, { assertion(it) })

}