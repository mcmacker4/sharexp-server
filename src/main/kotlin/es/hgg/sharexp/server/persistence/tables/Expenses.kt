package es.hgg.sharexp.server.persistence.tables

import es.hgg.sharexp.api.model.SplitMethod
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table


object Expenses : Table("expenses") {
    val groupId = reference("group_id", Groups.id, onDelete = ReferenceOption.CASCADE)
    val id = uuid("expense_id").autoGenerateV7()

    val title = varchar("title", 64)
    val description = varchar("description", 256).nullable()

    val paidBy = registerColumn("paid_by", GroupMembers.id.columnType)

    val splitMethod = enumeration<SplitMethod>("split_method")
    val amount = long("total_amount")

    override val primaryKey = PrimaryKey(groupId, id)

    init {
        foreignKey(groupId to GroupMembers.groupId, paidBy to GroupMembers.id, onDelete = ReferenceOption.RESTRICT)
    }
}

object ExpenseParticipants : Table("expense_participants") {
    val groupId = registerColumn("group_id", Groups.id.columnType)
    val expenseId = registerColumn("expense_id", Expenses.id.columnType)
    val memberId = registerColumn("member_id", GroupMembers.id.columnType)

    val configuredAmount = long("configured_amount")
    val computedAmount = long("computed_amount")

    override val primaryKey = PrimaryKey(groupId, expenseId, memberId)

    init {
        foreignKey(groupId to Expenses.groupId, expenseId to Expenses.id, onDelete = ReferenceOption.CASCADE)
        foreignKey(groupId to GroupMembers.groupId, memberId to GroupMembers.id)
    }
}