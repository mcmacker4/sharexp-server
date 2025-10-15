package es.hgg.sharexp.api.model

import kotlinx.serialization.Serializable

@Serializable
enum class RegisterError {
    MISSING_PARAM,
    USER_EXISTS,
    INVALID_USERNAME,
    WEAK_PASSWORD,
}

@Serializable
data class RegisterOutput(val error: RegisterError? = null)

