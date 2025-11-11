import es.hgg.sharexp.model.SplitMethod
import es.hgg.sharexp.server.ExpenseError
import es.hgg.sharexp.server.service.LuckyParticipantSelector
import es.hgg.sharexp.server.service.createExpenseSolver
import es.hgg.sharexp.server.util.UUIDv7
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid


class ExpenseSolversTest {

    companion object {
        private val BOB = UUIDv7.generate().toKotlinUuid()
        private val ALICE = UUIDv7.generate().toKotlinUuid()
        private val STEVE = UUIDv7.generate().toKotlinUuid()

        init {
            println("BOB   = $BOB")
            println("ALICE = $ALICE")
            println("STEVE = $STEVE")
        }
    }

    class RiggedLuckySelector(val participant: Uuid) : LuckyParticipantSelector {
        override fun select(paidBy: Uuid, participants: Set<Uuid>): Uuid = participant
    }

    @Test
    fun `all solvers, error when participant has amount 0`() {
        val participants = mapOf(ALICE to 1L, BOB to 0L)
        SplitMethod.entries.forEach { method ->
            val solver = createExpenseSolver(method)
            assertEquals(ExpenseError.InvalidParticipantAmount(setOf(BOB)), solver.validate(100L, participants), "Failed for method $method")
        }
    }

    @Test
    fun `all solvers, error when participant has negative amount`() {
        val participants = mapOf(ALICE to -1L, BOB to 0L)
        SplitMethod.entries.forEach { method ->
            val solver = createExpenseSolver(method)
            assertEquals(ExpenseError.InvalidParticipantAmount(setOf(ALICE, BOB)), solver.validate(100L, participants), "Failed for method $method")
        }
    }
    @Test
    fun `specific amounts resolves to the same amounts`() {
        val solver = createExpenseSolver(SplitMethod.AMOUNT)

        val amount = 6L
        val participants = mapOf(BOB to 1L, ALICE to 2L, STEVE to 3L)

        assertNull(solver.validate(amount, participants))
        assertEquals(participants, solver.solve(ALICE, amount, participants))
    }

    @Test
    fun `parts, rounding error pardoned to lucky person`() {
        val solver = createExpenseSolver(SplitMethod.PARTS)

        val amount = 6L
        val participants = mapOf(BOB to 1L, ALICE to 2L, STEVE to 2L)

        assertNull(solver.validate(amount, participants))
        val computed = solver.solve(ALICE, 1773L, participants)

        // There are 5 parts
        // ceil(1773 / 5) == 355
        // 355 * 5 = 1775
        // rounding error = 1775 - 1773 = 2
        // Alice is the lucky one
        // her part is (355 * 2 parts) - rounding error
        // (355 * 2) - 2 = 708

        assertEquals(participants.keys, computed.keys)

        assertEquals(355L, computed[BOB])
        assertEquals(708L, computed[ALICE])
        assertEquals(710L, computed[STEVE])
    }

    @Test
    fun `equal amounts, ignores input amounts`() {
        val solver = createExpenseSolver(SplitMethod.EQUAL)

        val amount = 6L
        val participants = mapOf(BOB to 1L, ALICE to 2L, STEVE to 3L)

        assertNull(solver.validate(amount, participants))
        val computed = solver.solve(ALICE, amount, participants)

        assertEquals(2L, computed[BOB])
        assertEquals(2L, computed[ALICE])
        assertEquals(2L, computed[STEVE])
    }

    @Test
    fun `equal amounts, pardons rounding errors to lucky person`() {
        val solver = createExpenseSolver(SplitMethod.EQUAL)

        val amount = 7L
        val participants = mapOf(BOB to 1L, ALICE to 1L, STEVE to 1L)

        assertNull(solver.validate(amount, participants))
        val computed = solver.solve(ALICE, amount, participants)

        assertEquals(3L, computed[BOB])
        assertEquals(1L, computed[ALICE])
        assertEquals(3L, computed[STEVE])
    }

    @Test
    fun `equal amounts, chooses random lucky when payer is not participant`() {
        val solver = createExpenseSolver(SplitMethod.EQUAL, RiggedLuckySelector(STEVE))

        val amount = 7L
        val participants = mapOf(BOB to 1L, STEVE to 1L)

        assertNull(solver.validate(amount, participants))
        val computed = solver.solve(ALICE, amount, participants)

        assertEquals(4L, computed[BOB])
        assertEquals(3L, computed[STEVE])
    }

}