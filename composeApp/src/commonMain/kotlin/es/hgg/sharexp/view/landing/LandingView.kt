package es.hgg.sharexp.view.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.hgg.sharexp.service.AuthenticationService
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
fun LandingPage(
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Loading...")
    }
}

@Preview
@Composable
private fun LandingPagePreview() {
    LandingPage({}, {})
}