package es.hgg.sharexp.server

import io.ktor.http.HttpStatusCode


sealed interface AppError {
    val status: HttpStatusCode

    object NotFound : AppError { override val status = HttpStatusCode.NotFound }
    object Forbidden : AppError { override val status = HttpStatusCode.Forbidden }
    object BadRequest : AppError { override val status = HttpStatusCode.BadRequest }
    object Conflict : AppError { override val status = HttpStatusCode.Conflict }
}
