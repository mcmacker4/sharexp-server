package es.hgg.sharexp.view.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginPage(
    onLoggedIn: () -> Unit,
    onRegisterRequest: () -> Unit,
) {
    val viewModel = koinViewModel<LoginViewModel>()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val currentOnLogin by rememberUpdatedState(onLoggedIn)

    val emailFieldState = rememberTextFieldState()
    val passFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        snapshotFlow { uiState.loginStatus }
            .filter { it == LoginStatus.Success }
            .collect { currentOnLogin() }
    }

    LoginForm(
        loginStatus = uiState.loginStatus,
        emailFieldState = emailFieldState,
        passFieldState = passFieldState,
        onLoginAttempt = {
            viewModel.attemptLogin(emailFieldState.text.toString(), passFieldState.text.toString())
        },
        onRegisterRequest = onRegisterRequest,
    )
}

@Composable
fun LoginForm(
    loginStatus: LoginStatus = LoginStatus.Pending,
    focusManager: FocusManager = LocalFocusManager.current,
    emailFieldState: TextFieldState = rememberTextFieldState(),
    passFieldState: TextFieldState = rememberTextFieldState(),
    onLoginAttempt: () -> Unit,
    onRegisterRequest: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {

        if (loginStatus is LoginStatus.Invalid) {
            AlertBox(loginStatus.message)
        }

        OutlinedTextField(
            emailFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            lineLimits = TextFieldLineLimits.SingleLine,
            isError = loginStatus is LoginStatus.Invalid,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email,
            ),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Next) }
        )

        OutlinedSecureTextField(
            passFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            isError = loginStatus is LoginStatus.Invalid,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go,
            ),
            onKeyboardAction = { onLoginAttempt() }
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(0.dp),
            onClick = { onLoginAttempt() }
        ) {
            Text("Submit")
        }

        TextButton(
            modifier = Modifier.fillMaxWidth().padding(0.dp),
            onClick = { onRegisterRequest() }
        ) {
            Text("Create Account")
        }
    }
}

@Composable
private fun AlertBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            fontSize = 4.em,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview
@Composable
private fun LoginPreview() {
    LoginForm(
        loginStatus = LoginStatus.Invalid("Hello Error"),
        onLoginAttempt = {},
        onRegisterRequest = {},
    )
}
