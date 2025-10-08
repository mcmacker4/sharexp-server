package es.hgg.sharexp.persistence.tables

import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.r2dbc.insertReturning


suspend fun<T : Table, R> T.insertReturningId(idColumn: Expression<R>, ignoreErrors: Boolean = false, body: T.(InsertStatement<Number>) -> Unit): R? {
    return insertReturning(
        returning = listOf(idColumn),
        ignoreErrors = ignoreErrors,
        body = body,
    ).singleOrNull()?.get(idColumn)
}