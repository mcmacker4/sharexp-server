package es.hgg.sharexp.view.register

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.hgg.sharexp.service.AuthenticationService
import es.hgg.sharexp.service.RegisterError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@Immutable
data class RegisterUiState(
    val success: Boolean = false,
    val errorMessage: String? = null,
)

class RegisterViewModel(
    val authService: AuthenticationService,
) : ViewModel() {

    private val success = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(success, errorMessage) { success, errorMessage ->
                RegisterUiState(success, errorMessage)
            }.collect { state ->
                _state.update { state }
            }
        }
    }

    fun attemptRegister(email: String, username: String, password: String) {
        viewModelScope.launch {
            authService.register(email, username, password).fold({ error ->
                errorMessage.update {
                    when (error) {
                        // TODO: Replace messages with (localized?) resource strings
                        RegisterError.EmptyField -> "Error 1"
                        RegisterError.Internal -> "Error 2"
                        RegisterError.InvalidUsername -> "Error 3"
                        RegisterError.UserExists -> "Error 4"
                        RegisterError.WeakPassword -> "Error 5"
                    }
                }
            }, {
                success.update { true }
            })
        }
    }

}