package es.hgg.sharexp.server

import io.ktor.http.HttpStatusCode
import java.util.UUID


sealed interface AppError {
    val status: HttpStatusCode

    object NotFound : AppError { override val status = HttpStatusCode.NotFound }
    object Forbidden : AppError { override val status = HttpStatusCode.Forbidden }
    object BadRequest : AppError { override val status = HttpStatusCode.BadRequest }
    object Conflict : AppError { override val status = HttpStatusCode.Conflict }
    object Internal : AppError { override val status = HttpStatusCode.InternalServerError }
}

sealed class ExpenseError(val message: String, val affectedParticipants: Set<UUID> = emptySet()) : AppError {
    override val status = HttpStatusCode.BadRequest

    object NoParticipants : ExpenseError("No participants selected", emptySet())
    object TooManyParts : ExpenseError("Too many parts to split amount", emptySet())
    object InvalidTotalAmount : ExpenseError("Invalid expense amount", emptySet())

    data class InexistentParticipants(val members: Set<UUID>) : ExpenseError("Members don't exist", members)
    data class InvalidParticipantAmount(val members: Set<UUID>) : ExpenseError("Invalid participant amount", members)
}

