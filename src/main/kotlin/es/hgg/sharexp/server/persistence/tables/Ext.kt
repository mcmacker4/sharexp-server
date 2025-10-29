package es.hgg.sharexp.server.persistence.tables

import es.hgg.sharexp.server.util.PageRequest
import es.hgg.sharexp.server.util.UUIDv7
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpsertStatement
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.upsertReturning
import java.util.*


suspend fun <T : Table, R> T.insertReturningId(
    idColumn: Expression<R>,
    ignoreErrors: Boolean = false,
    body: T.(InsertStatement<Number>) -> Unit
): R? {
    return insertReturning(
        returning = listOf(idColumn),
        ignoreErrors = ignoreErrors,
        body = body,
    ).singleOrNull()?.get(idColumn)
}

suspend fun <T : Table, R> T.upsertReturningId(
    idColumn: Expression<R>,
    onUpdateExclude: List<Column<*>>? = null,
    body: T.(UpsertStatement<Long>) -> Unit,
): R? {
    return upsertReturning(
        returning = listOf(idColumn),
        onUpdateExclude = onUpdateExclude,
        body = body,
    ).singleOrNull()?.get(idColumn)
}

context(table: T) fun <T : Table> Column<UUID>.autoGenerateV7(): Column<UUID> = with(table) {
    clientDefault { UUIDv7.generate() }
}

inline fun <S : Enum<S>> Query.page(request: PageRequest<S>, convertToColumn: (S) -> Expression<*>): Query = this
    .offset(request.offset)
    .limit(request.size)
    .orderBy(
        convertToColumn(request.sort),
        if (request.asc) SortOrder.ASC_NULLS_FIRST else SortOrder.DESC_NULLS_LAST
    )
