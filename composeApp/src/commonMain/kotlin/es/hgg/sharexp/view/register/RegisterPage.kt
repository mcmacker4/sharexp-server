package es.hgg.sharexp.view.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterPage(
    onRegistered: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel = koinViewModel<RegisterViewModel>()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val currentOnRegistered by rememberUpdatedState(onRegistered)

    val emailFieldState = rememberTextFieldState()
    val nameFieldState = rememberTextFieldState()
    val passFieldState = rememberTextFieldState()

    LaunchedEffect(uiState.success) {
        if (uiState.success)
            currentOnRegistered()
    }

    Column(
        verticalArrangement = Arrangement.Center,
    ) {
        RegisterForm(
            modifier = Modifier.safeContentPadding(),
            errorMessage = uiState.errorMessage,
            emailFieldState = emailFieldState,
            nameFieldState = nameFieldState,
            passFieldState = passFieldState,
            onRegister = {
                viewModel.attemptRegister(
                    emailFieldState.text.toString(),
                    nameFieldState.text.toString(),
                    passFieldState.text.toString()
                )
            }
        )
    }

}

@Composable
fun RegisterForm(
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    focusManager: FocusManager = LocalFocusManager.current,
    emailFieldState: TextFieldState = rememberTextFieldState(),
    nameFieldState: TextFieldState = rememberTextFieldState(),
    passFieldState: TextFieldState = rememberTextFieldState(),
    onRegister: () -> Unit,
) {
    Column(
        modifier = modifier.safeContentPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (errorMessage != null) {
            AlertBox(errorMessage)
        }

        OutlinedTextField(
            emailFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            lineLimits = TextFieldLineLimits.SingleLine,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email,
            ),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Next) }
        )

        OutlinedTextField(
            nameFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text,
            ),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Next) }
        )

        OutlinedSecureTextField(
            passFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go,
            ),
            onKeyboardAction = { onRegister() }
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(0.dp),
            onClick = { onRegister() }
        ) { Text("Submit") }
    }
}

@Composable
private fun AlertBox(message: String) {
    Text(
        message,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
            )
            .padding(12.dp),
        textAlign = TextAlign.Center,
        fontSize = 4.em,
        color = MaterialTheme.colorScheme.error,
    )
}

@Preview
@Composable
private fun LoginPreview() {
    RegisterForm(
        errorMessage = "Error message goes here"
    ) {}
}
