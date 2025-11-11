package es.hgg.sharexp.view.landing

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import es.hgg.sharexp.service.AuthenticationService
import org.koin.compose.koinInject

@Composable
fun LandingView(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val service = koinInject<AuthenticationService>()

    val currentOnLoggedIn by rememberUpdatedState(onLoggedIn)
    val currentOnLoggedOut by rememberUpdatedState(onLoggedOut)

    LaunchedEffect(Unit) {
        service.fetchAuthStatus().fold({
            currentOnLoggedOut()
        }, {
            currentOnLoggedIn()
        })
    }

    Column {
        Text("Loading...")
    }
}