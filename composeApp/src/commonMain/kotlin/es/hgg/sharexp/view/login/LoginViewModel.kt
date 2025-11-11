package es.hgg.sharexp.view.login

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.hgg.sharexp.service.AuthenticationService
import es.hgg.sharexp.service.LoginError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


sealed interface LoginStatus {
    object Pending : LoginStatus
    object Success : LoginStatus
    @Immutable
    data class Invalid(val message: String) : LoginStatus
}


@Immutable
data class LoginUiState(
    val loginStatus: LoginStatus = LoginStatus.Pending,
)

class LoginViewModel(
    val authService: AuthenticationService,
) : ViewModel() {

    private val loginStatus = MutableStateFlow<LoginStatus>(LoginStatus.Pending)

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loginStatus.collect { _state.value = LoginUiState(it) }
        }
    }

    fun attemptLogin(email: String, password: String) {
        viewModelScope.launch {
            authService.login(email, password).fold({ error ->
                loginStatus.update { error.intoLoginStatus() }
            }, {
                loginStatus.update { LoginStatus.Success }
            })
        }
    }

}

private fun LoginError.intoLoginStatus() = when (this) {
    // TODO: Replace messages with (localized?) resource strings
    LoginError.Authentication -> LoginStatus.Invalid("Wrong email or password")
    LoginError.Internal -> LoginStatus.Invalid("Something went wrong. Try again later...")
}